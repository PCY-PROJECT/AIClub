package com.aicode.module.code.controller;

import com.aicode.common.annotation.RateLimit;
import com.aicode.common.result.R;
import com.aicode.common.security.SecurityUtil;
import com.aicode.module.code.dto.ConfirmCodeRequest;
import com.aicode.module.code.dto.ContributeCodeRequest;
import com.aicode.module.code.service.InviteCodeService;
import com.aicode.module.code.vo.ClaimedCodeVO;
import com.aicode.module.code.vo.CodeItemVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/codes")
@RequiredArgsConstructor
public class InviteCodeController {

    private final InviteCodeService inviteCodeService;

    /** 贡献邀请码（需登录，限流10次/小时） */
    @PostMapping("/contribute")
    @RateLimit(key = "contribute_code", limit = 10, window = 3600, keySource = "USER",
               message = "贡献邀请码过于频繁，请稍后重试")
    public R<Void> contribute(@Valid @RequestBody ContributeCodeRequest request) {
        inviteCodeService.contributeCode(SecurityUtil.getCurrentUserId(), request);
        return R.ok();
    }

    /** 花积分从池中获取邀请码（需登录，限流5次/分钟） */
    @PostMapping("/{productId}/claim")
    @RateLimit(key = "claim_code", limit = 5, window = 60, keySource = "USER",
               message = "获取太频繁，每分钟最多5次")
    public R<ClaimedCodeVO> claim(@PathVariable Long productId) {
        return R.ok(inviteCodeService.claimCode(SecurityUtil.getCurrentUserId(), productId));
    }

    /** 确认邀请码有效性（需登录） */
    @PostMapping("/{id}/confirm")
    public R<Void> confirm(@PathVariable Long id, @Valid @RequestBody ConfirmCodeRequest request) {
        inviteCodeService.confirmCode(SecurityUtil.getCurrentUserId(), id, request.getResult());
        return R.ok();
    }

    /** 我贡献的邀请码列表（需登录） */
    @GetMapping("/mine")
    public R<Page<CodeItemVO>> mine(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return R.ok(inviteCodeService.myContributed(SecurityUtil.getCurrentUserId(), page, size));
    }

    /** 我获取的邀请码列表（需登录） */
    @GetMapping("/claimed")
    public R<Page<ClaimedCodeVO>> claimed(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return R.ok(inviteCodeService.myClaimed(SecurityUtil.getCurrentUserId(), page, size));
    }
}
