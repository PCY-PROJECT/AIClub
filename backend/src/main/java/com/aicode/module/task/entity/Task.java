package com.aicode.module.task.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_task")
public class Task {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String taskType;
    /** 1每日 2每周 3一次性 */
    private Integer frequency;
    private Integer pointsReward;
    /** 1启用 2停用 */
    private Integer status;
    private Integer sort;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
