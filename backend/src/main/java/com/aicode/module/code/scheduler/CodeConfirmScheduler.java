package com.aicode.module.code.scheduler;

import com.aicode.module.code.service.InviteCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CodeConfirmScheduler {

    private final InviteCodeService inviteCodeService;

    /**
     * 每10分钟扫描一次超时未确认的邀请码
     * 超时（12h内未确认）→ 自动执行 valid 逻辑（提供者 +30）
     */
    @Scheduled(fixedDelay = 600_000)
    public void processExpired() {
        log.debug("开始处理超时未确认邀请码...");
        try {
            inviteCodeService.processExpiredConfirmations();
        } catch (Exception e) {
            log.error("处理超时邀请码失败: {}", e.getMessage(), e);
        }
    }
}
