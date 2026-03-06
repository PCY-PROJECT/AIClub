package com.aicode.module.news.controller;

import com.aicode.common.result.R;
import com.aicode.common.security.SecurityUtil;
import com.aicode.module.news.dto.CreateNewsRequest;
import com.aicode.module.news.dto.SubmitNewsRequest;
import com.aicode.module.news.service.NewsService;
import com.aicode.module.news.vo.LikeResultVO;
import com.aicode.module.news.vo.NewsVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    /**
     * 资讯列表（公开）
     * GET /api/news?category=1&page=1&size=20
     */
    @GetMapping
    public R<Page<NewsVO>> list(
            @RequestParam(required = false) Integer category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = tryGetCurrentUserId();
        return R.ok(newsService.list(category, page, size, userId));
    }

    /**
     * 资讯详情（公开）
     * GET /api/news/{id}
     */
    @GetMapping("/{id}")
    public R<NewsVO> detail(@PathVariable Long id) {
        Long userId = tryGetCurrentUserId();
        return R.ok(newsService.detail(id, userId));
    }

    /**
     * 编辑直接创建并发布资讯（需登录）
     * POST /api/news
     */
    @PostMapping
    public R<Void> create(@RequestBody CreateNewsRequest req) {
        SecurityUtil.getCurrentUserId(); // 仅校验已登录
        newsService.create(req);
        return R.ok();
    }

    /**
     * 用户投稿资讯（需登录，提交后 status=待审核）
     * POST /api/news/submit
     */
    @PostMapping("/submit")
    public R<Void> submit(@RequestBody SubmitNewsRequest req) {
        Long userId = SecurityUtil.getCurrentUserId();
        newsService.submit(userId, req);
        return R.ok();
    }

    /**
     * 审核通过资讯并发放积分（需登录）
     * PUT /api/news/{id}/approve
     */
    @PutMapping("/{id}/approve")
    public R<Void> approve(@PathVariable Long id) {
        SecurityUtil.getCurrentUserId(); // 仅校验已登录
        newsService.approve(id);
        return R.ok();
    }

    /**
     * 点赞 / 取消点赞（需登录）
     * POST /api/news/{id}/like
     */
    @PostMapping("/{id}/like")
    public R<LikeResultVO> like(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        return R.ok(newsService.toggleLike(id, userId));
    }

    // ─── 工具方法：尝试获取当前用户 ID，未登录时返回 null ───────────────

    private Long tryGetCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long userId) {
            return userId;
        }
        return null;
    }
}
