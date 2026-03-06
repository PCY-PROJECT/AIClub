package com.aicode.module.skill.mapper;

import com.aicode.module.skill.entity.Skill;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SkillMapper extends BaseMapper<Skill> {

    @Update("UPDATE t_skill SET use_count = use_count + 1 WHERE id = #{id}")
    void incrementUseCount(Long id);

    @Update("UPDATE t_skill SET like_count = like_count + 1 WHERE id = #{id}")
    void incrementLikeCount(Long id);

    @Update("UPDATE t_skill SET like_count = GREATEST(like_count - 1, 0) WHERE id = #{id}")
    void decrementLikeCount(Long id);

    @Update("UPDATE t_skill SET collect_count = collect_count + 1 WHERE id = #{id}")
    void incrementCollectCount(Long id);

    @Update("UPDATE t_skill SET collect_count = GREATEST(collect_count - 1, 0) WHERE id = #{id}")
    void decrementCollectCount(Long id);
}
