package com.aicode.module.skill.controller;

import com.aicode.common.result.R;
import com.aicode.common.security.SecurityUtil;
import com.aicode.module.skill.dto.SubmitSkillRequest;
import com.aicode.module.skill.service.SkillService;
import com.aicode.module.skill.vo.SkillInteractVO;
import com.aicode.module.skill.vo.SkillVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    /**
     * 技能列表（公开）
     * GET /api/skills?category=营销&page=1&size=20
     */
    @GetMapping
    public R<Page<SkillVO>> list(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return R.ok(skillService.list(category, page, size, tryGetCurrentUserId()));
    }

    /**
     * 技能详情（公开），use_count+1
     * GET /api/skills/{id}
     */
    @GetMapping("/{id}")
    public R<SkillVO> detail(@PathVariable Long id) {
        return R.ok(skillService.detail(id, tryGetCurrentUserId()));
    }

    /**
     * 提交技能（需登录，status=待审核）
     * POST /api/skills
     */
    @PostMapping
    public R<Void> submit(@RequestBody SubmitSkillRequest req) {
        Long userId = SecurityUtil.getCurrentUserId();
        skillService.submit(userId, req);
        return R.ok();
    }

    /**
     * 审核通过（需登录）
     * PUT /api/skills/{id}/approve
     */
    @PutMapping("/{id}/approve")
    public R<Void> approve(@PathVariable Long id) {
        SecurityUtil.getCurrentUserId();
        skillService.approve(id);
        return R.ok();
    }

    /**
     * 点赞 / 取消点赞（需登录）
     * POST /api/skills/{id}/like
     */
    @PostMapping("/{id}/like")
    public R<SkillInteractVO> like(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        return R.ok(skillService.toggleLike(id, userId));
    }

    /**
     * 收藏 / 取消收藏（需登录）
     * POST /api/skills/{id}/collect
     */
    @PostMapping("/{id}/collect")
    public R<SkillInteractVO> collect(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        return R.ok(skillService.toggleCollect(id, userId));
    }

    /**
     * 我提交的技能（需登录）
     * GET /api/skills/mine
     */
    @GetMapping("/mine")
    public R<Page<SkillVO>> mine(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtil.getCurrentUserId();
        return R.ok(skillService.mySkills(userId, page, size));
    }

    private Long tryGetCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long userId) return userId;
        return null;
    }
}
