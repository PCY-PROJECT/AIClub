package com.aicode.module.mcp.controller;

import com.aicode.common.result.R;
import com.aicode.module.mcp.service.McpSkillService;
import com.aicode.module.mcp.vo.McpSkillVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mcp")
@RequiredArgsConstructor
public class McpSkillController {

    private final McpSkillService mcpSkillService;

    /**
     * Agent技能库列表（公开）
     * GET /api/mcp?type=1&category=文件管理&page=1&size=20
     */
    @GetMapping
    public R<Page<McpSkillVO>> list(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return R.ok(mcpSkillService.list(type, category, page, size));
    }

    /**
     * 详情（公开）
     * GET /api/mcp/{id}
     */
    @GetMapping("/{id}")
    public R<McpSkillVO> detail(@PathVariable Long id) {
        return R.ok(mcpSkillService.detail(id));
    }

    /**
     * 记录使用次数（公开，无需登录）
     * POST /api/mcp/{id}/use
     */
    @PostMapping("/{id}/use")
    public R<Void> recordUse(@PathVariable Long id) {
        mcpSkillService.recordUse(id);
        return R.ok();
    }
}
