package com.aicode.module.site.controller;

import com.aicode.common.result.R;
import com.aicode.common.security.SecurityUtil;
import com.aicode.module.site.dto.SubmitSiteRequest;
import com.aicode.module.site.entity.Site;
import com.aicode.module.site.mapper.SiteMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sites")
@RequiredArgsConstructor
public class SiteController {

    private final SiteMapper siteMapper;

    /** 已发布网站列表（公开），支持分类筛选 */
    @GetMapping
    public R<List<Site>> list(@RequestParam(required = false) String category) {
        LambdaQueryWrapper<Site> wrapper = new LambdaQueryWrapper<Site>()
                .eq(Site::getStatus, 2)
                .eq(category != null && !category.isBlank(), Site::getCategory, category)
                .orderByDesc(Site::getSort)
                .orderByDesc(Site::getCreateTime);
        return R.ok(siteMapper.selectList(wrapper));
    }

    /** 记录浏览（公开） */
    @PostMapping("/{id}/view")
    public R<Void> view(@PathVariable Long id) {
        Site site = siteMapper.selectById(id);
        if (site != null && site.getStatus() == 2) {
            site.setViewCount(site.getViewCount() + 1);
            siteMapper.updateById(site);
        }
        return R.ok();
    }

    /** 用户投稿（需登录） */
    @PostMapping("/submit")
    public R<Void> submit(@RequestBody SubmitSiteRequest req) {
        Long userId = SecurityUtil.getCurrentUserId();
        Site site = new Site();
        site.setName(req.getName());
        site.setDescription(req.getDescription());
        site.setUrl(req.getUrl());
        site.setLogo(req.getLogo());
        site.setCategory(req.getCategory());
        site.setTags(req.getTags());
        site.setStatus(1); // 待审核
        site.setSubmitUserId(userId);
        site.setViewCount(0);
        site.setSort(0);
        siteMapper.insert(site);
        return R.ok();
    }
}
