package com.aicode.module.skill.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_skill")
public class Skill {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    @TableField("`prompt`")          // prompt 是 MySQL 保留字，需要转义
    private String prompt;
    private String applicable;        // 适用产品，逗号分隔
    private String category;          // 写作/编程/设计/营销/分析/翻译/教育/职场/生活
    private String tags;              // 场景标签，逗号分隔
    private Long authorId;
    /** 1待审核 2已发布 3已拒绝 */
    private Integer status;
    private Integer likeCount;
    private Integer collectCount;
    private Integer useCount;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
