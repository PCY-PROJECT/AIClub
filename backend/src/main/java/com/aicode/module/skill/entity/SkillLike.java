package com.aicode.module.skill.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_skill_like")
public class SkillLike {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long skillId;
    private Long userId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
