package com.aicode.module.admin.controller;

import com.aicode.common.result.R;
import com.aicode.common.security.AdminUtil;
import com.aicode.common.security.SecurityUtil;
import com.aicode.module.admin.dto.AdminAdjustPointsRequest;
import com.aicode.module.admin.vo.AdminCodeVO;
import com.aicode.module.admin.vo.AdminUserVO;
import com.aicode.module.code.entity.InviteCode;
import com.aicode.module.code.mapper.InviteCodeMapper;
import com.aicode.module.mcp.dto.CreateMcpSkillRequest;
import com.aicode.module.product.dto.CreateProductRequest;
import com.aicode.module.mcp.service.McpSkillService;
import com.aicode.module.mcp.vo.McpSkillVO;
import com.aicode.module.news.service.NewsService;
import com.aicode.module.news.vo.NewsVO;
import com.aicode.module.point.service.PointService;
import com.aicode.module.product.entity.Product;
import com.aicode.module.product.mapper.ProductMapper;
import com.aicode.module.resource.dto.CreateResourceRequest;
import com.aicode.module.resource.service.ResourceService;
import com.aicode.module.resource.vo.ResourceVO;
import com.aicode.module.skill.service.SkillService;
import com.aicode.module.skill.vo.SkillVO;
import com.aicode.module.user.entity.User;
import com.aicode.module.site.entity.Site;
import com.aicode.module.site.mapper.SiteMapper;
import com.aicode.module.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 后台管理 API — 所有接口需管理员身份（role=9）
 * 路由前缀：/api/admin
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminUtil adminUtil;
    private final ResourceService resourceService;
    private final McpSkillService mcpSkillService;
    private final NewsService newsService;
    private final SkillService skillService;
    private final PointService pointService;
    private final UserMapper userMapper;
    private final InviteCodeMapper inviteCodeMapper;
    private final ProductMapper productMapper;
    private final SiteMapper siteMapper;

    // ═══════════════════════════════════════════════════════════
    // 知识库管理
    // ═══════════════════════════════════════════════════════════

    /** 知识库列表（按状态筛选）GET /api/admin/resources?status=1&page=1&size=20 */
    @GetMapping("/resources")
    public R<Page<ResourceVO>> listResources(
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        requireAdmin();
        // 管理员视图：传 null 作为 currentUserId，status 由 service 支持（这里直接用全量接口）
        // 简化：status 参数通过调整 category/difficulty 传 null，status 过滤
        return R.ok(resourceService.listAdmin(category, status, page, size));
    }

    /** 创建并发布知识库内容 POST /api/admin/resources */
    @PostMapping("/resources")
    public R<Void> createResource(@RequestBody CreateResourceRequest req) {
        requireAdmin();
        resourceService.create(req);
        return R.ok();
    }

    /** 审核通过 PUT /api/admin/resources/{id}/approve */
    @PutMapping("/resources/{id}/approve")
    public R<Void> approveResource(@PathVariable Long id) {
        requireAdmin();
        resourceService.approve(id);
        return R.ok();
    }

    /** 审核拒绝 PUT /api/admin/resources/{id}/reject */
    @PutMapping("/resources/{id}/reject")
    public R<Void> rejectResource(@PathVariable Long id) {
        requireAdmin();
        resourceService.reject(id);
        return R.ok();
    }

    // ═══════════════════════════════════════════════════════════
    // 提示词管理
    // ═══════════════════════════════════════════════════════════

    /** 提示词列表（按状态筛选）GET /api/admin/prompts?status=1&page=1&size=20 */
    @GetMapping("/prompts")
    public R<Page<SkillVO>> listPrompts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        requireAdmin();
        return R.ok(skillService.listAdmin(category, status, page, size));
    }

    /** 审核通过 PUT /api/admin/prompts/{id}/approve */
    @PutMapping("/prompts/{id}/approve")
    public R<Void> approvePrompt(@PathVariable Long id) {
        requireAdmin();
        skillService.approve(id);
        return R.ok();
    }

    /** 审核拒绝 PUT /api/admin/prompts/{id}/reject */
    @PutMapping("/prompts/{id}/reject")
    public R<Void> rejectPrompt(@PathVariable Long id) {
        requireAdmin();
        skillService.reject(id);
        return R.ok();
    }

    // ═══════════════════════════════════════════════════════════
    // Agent技能库管理
    // ═══════════════════════════════════════════════════════════

    /** MCP技能列表（含下架）GET /api/admin/mcp?type=1&status=2&page=1&size=20 */
    @GetMapping("/mcp")
    public R<Page<McpSkillVO>> listMcp(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        requireAdmin();
        return R.ok(mcpSkillService.listAdmin(type, category, status, page, size));
    }

    /** 创建MCP条目 POST /api/admin/mcp */
    @PostMapping("/mcp")
    public R<Void> createMcp(@RequestBody CreateMcpSkillRequest req) {
        requireAdmin();
        mcpSkillService.create(req);
        return R.ok();
    }

    /** 更新MCP条目 PUT /api/admin/mcp/{id} */
    @PutMapping("/mcp/{id}")
    public R<Void> updateMcp(@PathVariable Long id, @RequestBody CreateMcpSkillRequest req) {
        requireAdmin();
        mcpSkillService.update(id, req);
        return R.ok();
    }

    /** 上/下架MCP条目 PUT /api/admin/mcp/{id}/status?status=2 */
    @PutMapping("/mcp/{id}/status")
    public R<Void> updateMcpStatus(@PathVariable Long id, @RequestParam Integer status) {
        requireAdmin();
        mcpSkillService.updateStatus(id, status);
        return R.ok();
    }

    // ═══════════════════════════════════════════════════════════
    // 资讯管理
    // ═══════════════════════════════════════════════════════════

    /** 资讯列表（按状态）GET /api/admin/news?status=1&page=1&size=20 */
    @GetMapping("/news")
    public R<Page<NewsVO>> listNews(
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        requireAdmin();
        return R.ok(newsService.listAdmin(category, status, page, size));
    }

    /** 审核通过 PUT /api/admin/news/{id}/approve */
    @PutMapping("/news/{id}/approve")
    public R<Void> approveNews(@PathVariable Long id) {
        requireAdmin();
        newsService.approve(id);
        return R.ok();
    }

    /** 审核拒绝 PUT /api/admin/news/{id}/reject */
    @PutMapping("/news/{id}/reject")
    public R<Void> rejectNews(@PathVariable Long id) {
        requireAdmin();
        newsService.reject(id);
        return R.ok();
    }

    /** 下线资讯 PUT /api/admin/news/{id}/offline */
    @PutMapping("/news/{id}/offline")
    public R<Void> offlineNews(@PathVariable Long id) {
        requireAdmin();
        newsService.offline(id);
        return R.ok();
    }

    // ═══════════════════════════════════════════════════════════
    // 用户管理
    // ═══════════════════════════════════════════════════════════

    /** 用户列表 GET /api/admin/users?page=1&size=20 */
    @GetMapping("/users")
    public R<Page<AdminUserVO>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        requireAdmin();
        Page<User> userPage = userMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<User>().orderByDesc(User::getCreateTime));
        Page<AdminUserVO> voPage = new Page<>(page, size, userPage.getTotal());
        voPage.setRecords(userPage.getRecords().stream().map(this::toUserVO).toList());
        return R.ok(voPage);
    }

    /** 积分调整 POST /api/admin/users/points */
    @PostMapping("/users/points")
    public R<Void> adjustPoints(@RequestBody AdminAdjustPointsRequest req) {
        requireAdmin();
        if (req.getDelta() == null || req.getDelta() == 0) {
            return R.fail("delta 不能为0");
        }
        String remark = req.getRemark() != null ? req.getRemark() : "管理员手动调整";
        pointService.addPoints(req.getUserId(), req.getDelta(), 99, null, remark);
        return R.ok();
    }

    /** 封禁/解封用户 PUT /api/admin/users/{id}/status?status=2 */
    @PutMapping("/users/{id}/status")
    public R<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        requireAdmin();
        if (status != 1 && status != 2) return R.fail("status 只能是 1(正常) 或 2(封禁)");
        User user = userMapper.selectById(id);
        if (user == null) return R.fail("用户不存在");
        user.setStatus(status);
        userMapper.updateById(user);
        return R.ok();
    }

    // ═══════════════════════════════════════════════════════════
    // 邀请码管理
    // ═══════════════════════════════════════════════════════════

    /** 邀请码列表 GET /api/admin/codes?productId=&status=&page=&size= */
    @GetMapping("/codes")
    public R<Page<AdminCodeVO>> listCodes(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        requireAdmin();
        LambdaQueryWrapper<InviteCode> wrapper = new LambdaQueryWrapper<InviteCode>()
                .eq(productId != null, InviteCode::getProductId, productId)
                .eq(status != null, InviteCode::getStatus, status)
                .orderByDesc(InviteCode::getCreateTime);
        Page<InviteCode> codePage = inviteCodeMapper.selectPage(new Page<>(page, size), wrapper);

        if (codePage.getRecords().isEmpty()) {
            return R.ok(new Page<>(page, size, 0));
        }

        Set<Long> pids = codePage.getRecords().stream().map(InviteCode::getProductId).collect(Collectors.toSet());
        Set<Long> uids = codePage.getRecords().stream().map(InviteCode::getProviderId).collect(Collectors.toSet());
        // 获取者ID（可能为null）
        Set<Long> claimantIds = codePage.getRecords().stream()
                .filter(c -> c.getClaimantId() != null)
                .map(InviteCode::getClaimantId).collect(Collectors.toSet());
        uids.addAll(claimantIds);

        Map<Long, String> productNameMap = productMapper.selectBatchIds(pids)
                .stream().collect(Collectors.toMap(Product::getId, Product::getName));
        Map<Long, String> nicknameMap = uids.isEmpty() ? Map.of() :
                userMapper.selectBatchIds(uids)
                .stream().collect(Collectors.toMap(User::getId, User::getNickname));

        Page<AdminCodeVO> voPage = new Page<>(page, size, codePage.getTotal());
        voPage.setRecords(codePage.getRecords().stream().map(c -> {
            AdminCodeVO vo = new AdminCodeVO();
            vo.setId(c.getId());
            vo.setProductId(c.getProductId());
            vo.setProductName(productNameMap.get(c.getProductId()));
            vo.setProviderId(c.getProviderId());
            vo.setProviderNickname(nicknameMap.get(c.getProviderId()));
            vo.setCodePreview(c.getCodePreview());
            vo.setStatus(c.getStatus());
            vo.setCreateTime(c.getCreateTime());
            vo.setClaimantId(c.getClaimantId());
            if (c.getClaimantId() != null) {
                vo.setClaimantNickname(nicknameMap.get(c.getClaimantId()));
            }
            vo.setClaimTime(c.getClaimTime());
            vo.setConfirmDeadline(c.getConfirmDeadline());
            vo.setConfirmResult(c.getConfirmResult());
            vo.setReviewTime(c.getReviewTime());
            return vo;
        }).toList());
        return R.ok(voPage);
    }

    /** 审核通过（status 5→1）PUT /api/admin/codes/{id}/approve */
    @PutMapping("/codes/{id}/approve")
    public R<Void> approveCode(@PathVariable Long id) {
        requireAdmin();
        InviteCode code = inviteCodeMapper.selectById(id);
        if (code == null) return R.fail("邀请码不存在");
        if (code.getStatus() != 5) return R.fail("只有待审核的邀请码才能通过");
        Long adminId = SecurityUtil.getCurrentUserId();
        code.setStatus(1);
        code.setReviewerId(adminId);
        code.setReviewTime(java.time.LocalDateTime.now());
        inviteCodeMapper.updateById(code);
        // 审核通过后增加产品可用数量
        productMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Product>()
                        .eq(Product::getId, code.getProductId())
                        .setSql("code_count = code_count + 1"));
        return R.ok();
    }

    /** 审核拒绝（status 5→6）PUT /api/admin/codes/{id}/reject */
    @PutMapping("/codes/{id}/reject")
    public R<Void> rejectCode(@PathVariable Long id) {
        requireAdmin();
        InviteCode code = inviteCodeMapper.selectById(id);
        if (code == null) return R.fail("邀请码不存在");
        if (code.getStatus() != 5) return R.fail("只有待审核的邀请码才能拒绝");
        Long adminId = SecurityUtil.getCurrentUserId();
        code.setStatus(6);
        code.setReviewerId(adminId);
        code.setReviewTime(java.time.LocalDateTime.now());
        inviteCodeMapper.updateById(code);
        return R.ok();
    }

    // ═══════════════════════════════════════════════════════════
    // 产品管理
    // ═══════════════════════════════════════════════════════════

    /** 产品列表（含下线）GET /api/admin/products?status=&page=&size= */
    @GetMapping("/products")
    public R<Page<Product>> listProducts(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        requireAdmin();
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(status != null, Product::getStatus, status)
                .orderByDesc(Product::getSort)
                .orderByDesc(Product::getCreateTime);
        return R.ok(productMapper.selectPage(new Page<>(page, size), wrapper));
    }

    /** 创建产品 POST /api/admin/products */
    @PostMapping("/products")
    public R<Void> createProduct(@RequestBody CreateProductRequest req) {
        requireAdmin();
        if (req.getName() == null || req.getName().isBlank()) return R.fail("产品名称不能为空");
        Product product = new Product();
        product.setName(req.getName());
        product.setDescription(req.getDescription());
        product.setOfficialUrl(req.getOfficialUrl());
        product.setLogo(req.getLogo());
        product.setCategory(req.getCategory() != null ? req.getCategory() : 9);
        product.setStatus(1);
        product.setSort(req.getSort() != null ? req.getSort() : 0);
        product.setCodeCount(0);
        productMapper.insert(product);
        return R.ok();
    }

    /** 更新产品 PUT /api/admin/products/{id} */
    @PutMapping("/products/{id}")
    public R<Void> updateProduct(@PathVariable Long id, @RequestBody CreateProductRequest req) {
        requireAdmin();
        Product product = productMapper.selectById(id);
        if (product == null) return R.fail("产品不存在");
        if (req.getName() != null && !req.getName().isBlank()) product.setName(req.getName());
        if (req.getDescription() != null) product.setDescription(req.getDescription());
        if (req.getOfficialUrl() != null) product.setOfficialUrl(req.getOfficialUrl());
        if (req.getLogo() != null) product.setLogo(req.getLogo());
        if (req.getCategory() != null) product.setCategory(req.getCategory());
        if (req.getSort() != null) product.setSort(req.getSort());
        productMapper.updateById(product);
        return R.ok();
    }

    /** 上/下线产品 PUT /api/admin/products/{id}/status?status=1 */
    @PutMapping("/products/{id}/status")
    public R<Void> updateProductStatus(@PathVariable Long id, @RequestParam Integer status) {
        requireAdmin();
        if (status != 1 && status != 2) return R.fail("status 只能是 1(上线) 或 2(下线)");
        Product product = productMapper.selectById(id);
        if (product == null) return R.fail("产品不存在");
        product.setStatus(status);
        productMapper.updateById(product);
        return R.ok();
    }

    // ─── 私有方法 ────────────────────────────────────────────────

    private void requireAdmin() {
        Long userId = SecurityUtil.getCurrentUserId();
        adminUtil.requireAdmin(userId);
    }

    private AdminUserVO toUserVO(User u) {
        AdminUserVO vo = new AdminUserVO();
        vo.setId(u.getId());
        vo.setEmail(u.getEmail());
        vo.setNickname(u.getNickname());
        vo.setPoints(u.getPoints());
        vo.setStatus(u.getStatus());
        vo.setRole(u.getRole());
        vo.setUserNo(u.getUserNo());
        vo.setInviteCode(u.getInviteCode());
        vo.setRegIp(u.getRegIp());
        vo.setCreateTime(u.getCreateTime());
        return vo;
    }

    // ═══════════════════════════════════════════════════════════
    // AI 网站导航管理
    // ═══════════════════════════════════════════════════════════

    /** 网站列表（按状态筛选）GET /api/admin/sites?status=1&page=1&size=20 */
    @GetMapping("/sites")
    public R<Page<Site>> listSites(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        adminUtil.requireAdmin(SecurityUtil.getCurrentUserId());
        LambdaQueryWrapper<Site> wrapper = new LambdaQueryWrapper<Site>()
                .eq(status != null, Site::getStatus, status)
                .orderByDesc(Site::getCreateTime);
        return R.ok(siteMapper.selectPage(new Page<>(page, size), wrapper));
    }

    /** 审核通过 POST /api/admin/sites/{id}/approve */
    @PostMapping("/sites/{id}/approve")
    public R<Void> approveSite(@PathVariable Long id) {
        adminUtil.requireAdmin(SecurityUtil.getCurrentUserId());
        Site site = siteMapper.selectById(id);
        if (site == null) return R.fail(404, "网站不存在");
        site.setStatus(2);
        siteMapper.updateById(site);
        return R.ok();
    }

    /** 审核拒绝 POST /api/admin/sites/{id}/reject */
    @PostMapping("/sites/{id}/reject")
    public R<Void> rejectSite(@PathVariable Long id) {
        adminUtil.requireAdmin(SecurityUtil.getCurrentUserId());
        Site site = siteMapper.selectById(id);
        if (site == null) return R.fail(404, "网站不存在");
        site.setStatus(3);
        siteMapper.updateById(site);
        return R.ok();
    }

    /** 删除网站 DELETE /api/admin/sites/{id} */
    @DeleteMapping("/sites/{id}")
    public R<Void> deleteSite(@PathVariable Long id) {
        adminUtil.requireAdmin(SecurityUtil.getCurrentUserId());
        siteMapper.deleteById(id);
        return R.ok();
    }
}
