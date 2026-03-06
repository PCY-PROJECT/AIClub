package com.aicode.module.resource.controller;

import com.aicode.common.result.R;
import com.aicode.common.security.SecurityUtil;
import com.aicode.module.resource.dto.CreateResourceRequest;
import com.aicode.module.resource.dto.SubmitResourceRequest;
import com.aicode.module.resource.service.ResourceService;
import com.aicode.module.resource.vo.CollectResultVO;
import com.aicode.module.resource.vo.ResourceVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    /**
     * 知识库列表（公开）
     * GET /api/resources?category=1&difficulty=1&page=1&size=20
     */
    @GetMapping
    public R<Page<ResourceVO>> list(
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = tryGetCurrentUserId();
        return R.ok(resourceService.list(category, difficulty, page, size, userId));
    }

    /**
     * 知识库详情（公开，记录浏览量）
     * GET /api/resources/{id}
     */
    @GetMapping("/{id}")
    public R<ResourceVO> detail(@PathVariable Long id) {
        Long userId = tryGetCurrentUserId();
        return R.ok(resourceService.detail(id, userId));
    }

    /**
     * 编辑直接创建并发布（需登录）
     * POST /api/resources
     */
    @PostMapping
    public R<Void> create(@RequestBody CreateResourceRequest req) {
        SecurityUtil.getCurrentUserId();
        resourceService.create(req);
        return R.ok();
    }

    /**
     * 用户投稿（需登录，status=待审核）
     * POST /api/resources/submit
     */
    @PostMapping("/submit")
    public R<Void> submit(@RequestBody SubmitResourceRequest req) {
        Long userId = SecurityUtil.getCurrentUserId();
        resourceService.submit(userId, req);
        return R.ok();
    }

    /**
     * 收藏 / 取消收藏（需登录）
     * POST /api/resources/{id}/collect
     */
    @PostMapping("/{id}/collect")
    public R<CollectResultVO> collect(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        return R.ok(resourceService.toggleCollect(id, userId));
    }

    private Long tryGetCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long userId) {
            return userId;
        }
        return null;
    }
}
