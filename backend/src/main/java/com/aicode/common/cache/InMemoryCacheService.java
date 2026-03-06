package com.aicode.common.cache;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 基于 JVM 内存的轻量缓存服务，替代 Redis 用于验证码、限流计数等短期 KV 存储。
 * 适用于单机部署场景，重启后数据清空（影响可接受）。
 */
@Service
public class InMemoryCacheService {

    private static final class Entry {
        final String value;
        final long expireAt; // epoch millis，-1 表示永不过期

        Entry(String value, long expireAt) {
            this.value = value;
            this.expireAt = expireAt;
        }

        boolean isExpired() {
            return expireAt != -1 && System.currentTimeMillis() > expireAt;
        }
    }

    private final ConcurrentHashMap<String, Entry> store = new ConcurrentHashMap<>();

    @PostConstruct
    void startCleanup() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "mem-cache-cleanup");
            t.setDaemon(true);
            return t;
        });
        // 每 2 分钟清理一次过期条目，避免内存泄漏
        scheduler.scheduleAtFixedRate(
                () -> store.entrySet().removeIf(e -> e.getValue().isExpired()),
                2, 2, TimeUnit.MINUTES
        );
    }

    /** 存入带 TTL 的键值 */
    public void set(String key, String value, Duration ttl) {
        store.put(key, new Entry(value, System.currentTimeMillis() + ttl.toMillis()));
    }

    /** 获取值，过期或不存在返回 null */
    public String get(String key) {
        Entry e = store.get(key);
        if (e == null || e.isExpired()) {
            if (e != null) store.remove(key);
            return null;
        }
        return e.value;
    }

    /** 判断 key 是否存在且未过期 */
    public boolean hasKey(String key) {
        return get(key) != null;
    }

    /** 删除 key */
    public void delete(String key) {
        store.remove(key);
    }

    /**
     * 仅当 key 不存在（或已过期）时才设置，成功返回 true，已存在返回 false。
     * 原子操作，可用于签到去重等幂等场景。
     */
    public boolean setIfAbsent(String key, String value, Duration ttl) {
        long expireAt = System.currentTimeMillis() + ttl.toMillis();
        boolean[] inserted = {false};
        store.compute(key, (k, existing) -> {
            if (existing == null || existing.isExpired()) {
                inserted[0] = true;
                return new Entry(value, expireAt);
            }
            return existing;
        });
        return inserted[0];
    }

    /**
     * 对已存在的 key 进行原子递增，返回递增后的值。
     * key 不存在时从 1 开始，保留原 TTL（由首次 set 设置）。
     */
    public long increment(String key) {
        long[] result = {1};
        store.compute(key, (k, existing) -> {
            if (existing == null || existing.isExpired()) {
                return new Entry("1", -1L);
            }
            long val = Long.parseLong(existing.value) + 1;
            result[0] = val;
            return new Entry(String.valueOf(val), existing.expireAt);
        });
        return result[0];
    }
}
