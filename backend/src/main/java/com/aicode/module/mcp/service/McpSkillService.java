package com.aicode.module.mcp.service;

import com.aicode.module.mcp.dto.CreateMcpSkillRequest;
import com.aicode.module.mcp.vo.McpSkillVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface McpSkillService {

    Page<McpSkillVO> list(Integer type, String category, int pageNum, int pageSize);

    McpSkillVO detail(Long id);

    void create(CreateMcpSkillRequest req);

    void update(Long id, CreateMcpSkillRequest req);

    void updateStatus(Long id, Integer status);

    void recordUse(Long id);

    /** 管理员列表（含下架状态，可按 status 筛选） */
    Page<McpSkillVO> listAdmin(Integer type, String category, Integer status, int pageNum, int pageSize);
}
