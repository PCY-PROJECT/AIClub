package com.aicode.module.mcp.service.impl;

import com.aicode.common.exception.BizException;
import com.aicode.module.mcp.dto.CreateMcpSkillRequest;
import com.aicode.module.mcp.entity.McpSkill;
import com.aicode.module.mcp.mapper.McpSkillMapper;
import com.aicode.module.mcp.service.McpSkillService;
import com.aicode.module.mcp.vo.McpSkillVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class McpSkillServiceImpl implements McpSkillService {

    private final McpSkillMapper mcpSkillMapper;

    private static final String[] TYPE_NAMES = {"", "MCP Server", "Agent Skill"};

    @Override
    public Page<McpSkillVO> list(Integer type, String category, int pageNum, int pageSize) {
        LambdaQueryWrapper<McpSkill> wrapper = new LambdaQueryWrapper<McpSkill>()
                .eq(McpSkill::getStatus, 2)
                .eq(type != null && type != 0, McpSkill::getType, type)
                .eq(category != null && !category.isBlank(), McpSkill::getCategory, category)
                .orderByDesc(McpSkill::getSort)
                .orderByDesc(McpSkill::getCollectCount);

        Page<McpSkill> page = mcpSkillMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        Page<McpSkillVO> voPage = new Page<>(pageNum, pageSize, page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public McpSkillVO detail(Long id) {
        McpSkill skill = mcpSkillMapper.selectById(id);
        if (skill == null || skill.getStatus() != 2) {
            throw new BizException(404, "内容不存在");
        }
        return toVO(skill);
    }

    @Override
    @Transactional
    public void create(CreateMcpSkillRequest req) {
        validateFields(req.getName(), req.getType());
        McpSkill skill = buildSkill(req);
        skill.setStatus(2);  // 编辑直接上架
        skill.setCollectCount(0);
        skill.setUseCount(0);
        mcpSkillMapper.insert(skill);
    }

    @Override
    @Transactional
    public void update(Long id, CreateMcpSkillRequest req) {
        McpSkill skill = mcpSkillMapper.selectById(id);
        if (skill == null) throw new BizException(404, "内容不存在");
        validateFields(req.getName(), req.getType());
        skill.setName(req.getName());
        skill.setDescription(req.getDescription());
        skill.setType(req.getType());
        skill.setCategory(req.getCategory());
        skill.setSourceUrl(req.getSourceUrl());
        skill.setInstallGuide(req.getInstallGuide());
        skill.setVendor(req.getVendor());
        skill.setTags(req.getTags());
        if (req.getSort() != null) skill.setSort(req.getSort());
        mcpSkillMapper.updateById(skill);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        McpSkill skill = mcpSkillMapper.selectById(id);
        if (skill == null) throw new BizException(404, "内容不存在");
        if (status == null || status < 1 || status > 3) throw new BizException(400, "状态参数有误");
        skill.setStatus(status);
        mcpSkillMapper.updateById(skill);
    }

    @Override
    @Transactional
    public void recordUse(Long id) {
        McpSkill skill = mcpSkillMapper.selectById(id);
        if (skill == null || skill.getStatus() != 2) throw new BizException(404, "内容不存在");
        mcpSkillMapper.incrementUseCount(id);
    }

    // ─── 私有方法 ────────────────────────────────────────────────

    private McpSkill buildSkill(CreateMcpSkillRequest req) {
        McpSkill skill = new McpSkill();
        skill.setName(req.getName());
        skill.setDescription(req.getDescription());
        skill.setType(req.getType());
        skill.setCategory(req.getCategory());
        skill.setSourceUrl(req.getSourceUrl());
        skill.setInstallGuide(req.getInstallGuide());
        skill.setVendor(req.getVendor());
        skill.setTags(req.getTags());
        skill.setSort(req.getSort() != null ? req.getSort() : 0);
        return skill;
    }

    private McpSkillVO toVO(McpSkill skill) {
        McpSkillVO vo = new McpSkillVO();
        vo.setId(skill.getId());
        vo.setName(skill.getName());
        vo.setDescription(skill.getDescription());
        vo.setType(skill.getType());
        if (skill.getType() != null && skill.getType() >= 1 && skill.getType() <= 2) {
            vo.setTypeName(TYPE_NAMES[skill.getType()]);
        }
        vo.setCategory(skill.getCategory());
        vo.setSourceUrl(skill.getSourceUrl());
        vo.setInstallGuide(skill.getInstallGuide());
        vo.setVendor(skill.getVendor());
        vo.setTags(skill.getTags());
        vo.setStatus(skill.getStatus());
        vo.setCollectCount(skill.getCollectCount());
        vo.setUseCount(skill.getUseCount());
        vo.setSort(skill.getSort());
        vo.setCreateTime(skill.getCreateTime());
        return vo;
    }

    private void validateFields(String name, Integer type) {
        if (name == null || name.isBlank()) throw new BizException(400, "名称不能为空");
        if (type == null || type < 1 || type > 2) throw new BizException(400, "类型参数有误，1=MCP Server 2=Agent Skill");
    }

    // ─── Admin 方法 ────────────────────────────────────────────

    @Override
    public Page<McpSkillVO> listAdmin(Integer type, String category, Integer status, int pageNum, int pageSize) {
        LambdaQueryWrapper<McpSkill> wrapper = new LambdaQueryWrapper<McpSkill>()
                .eq(type != null && type != 0, McpSkill::getType, type)
                .eq(category != null && !category.isBlank(), McpSkill::getCategory, category)
                .eq(status != null, McpSkill::getStatus, status)
                .orderByDesc(McpSkill::getCreateTime);
        Page<McpSkill> page = mcpSkillMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Page<McpSkillVO> voPage = new Page<>(pageNum, pageSize, page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return voPage;
    }
}
