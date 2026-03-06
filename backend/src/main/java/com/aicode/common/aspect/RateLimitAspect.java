package com.aicode.common.aspect;

import com.aicode.common.annotation.RateLimit;
import com.aicode.common.exception.BizException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 基于 JVM 内存的接口限流切面（滑动窗口算法）。
 * 使用 ConcurrentHashMap + synchronized(deque) 保证线程安全。
 */
@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    // key -> 请求时间戳队列（毫秒，升序）
    private final ConcurrentHashMap<String, Deque<Long>> windows = new ConcurrentHashMap<>();

    @PostConstruct
    void startCleanup() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "rate-limit-cleanup");
            t.setDaemon(true);
            return t;
        });
        // 每 5 分钟清理空窗口，防止 map 无限增长
        scheduler.scheduleAtFixedRate(() -> {
            long cutoff = System.currentTimeMillis() - 3600_000L; // 保留近1小时
            windows.entrySet().removeIf(entry -> {
                Deque<Long> deque = entry.getValue();
                synchronized (deque) {
                    return deque.isEmpty() || deque.peekLast() < cutoff;
                }
            });
        }, 5, 5, TimeUnit.MINUTES);
    }

    @Before("@annotation(rateLimit)")
    public void check(RateLimit rateLimit) {
        String id = resolveId(rateLimit.keySource());
        String windowKey = "rl:" + rateLimit.key() + ":" + id;

        if (!isAllowed(windowKey, rateLimit.limit(), rateLimit.window())) {
            throw new BizException(429, rateLimit.message());
        }
    }

    /**
     * 滑动窗口限流判断。
     *
     * @param key     限流 key
     * @param limit   窗口内最大请求数
     * @param windowSec 窗口时长（秒）
     * @return true=允许，false=拒绝
     */
    private boolean isAllowed(String key, int limit, int windowSec) {
        long now = System.currentTimeMillis();
        long windowStart = now - (long) windowSec * 1000;

        Deque<Long> deque = windows.computeIfAbsent(key, k -> new ArrayDeque<>());

        synchronized (deque) {
            // 移除窗口外的旧时间戳
            while (!deque.isEmpty() && deque.peekFirst() < windowStart) {
                deque.pollFirst();
            }
            if (deque.size() >= limit) {
                return false;
            }
            deque.addLast(now);
            return true;
        }
    }

    private String resolveId(String keySource) {
        if ("USER".equals(keySource)) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Long userId) {
                return String.valueOf(userId);
            }
            return "anonymous";
        }
        return getClientIp();
    }

    private String getClientIp() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return "unknown";
        HttpServletRequest request = attrs.getRequest();
        String ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isBlank()) ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
        if (ip != null && ip.contains(",")) ip = ip.split(",")[0].trim();
        return ip;
    }
}
