package com.aicode.module.mcp.mapper;

import com.aicode.module.mcp.entity.McpSkill;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface McpSkillMapper extends BaseMapper<McpSkill> {

    @Update("UPDATE t_mcp_skill SET use_count = use_count + 1 WHERE id = #{id}")
    void incrementUseCount(Long id);

    @Update("UPDATE t_mcp_skill SET collect_count = collect_count + 1 WHERE id = #{id}")
    void incrementCollectCount(Long id);

    @Update("UPDATE t_mcp_skill SET collect_count = GREATEST(collect_count - 1, 0) WHERE id = #{id}")
    void decrementCollectCount(Long id);
}
