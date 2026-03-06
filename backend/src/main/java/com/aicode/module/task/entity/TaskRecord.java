package com.aicode.module.task.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_task_record")
public class TaskRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long taskId;
    private String taskType;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
