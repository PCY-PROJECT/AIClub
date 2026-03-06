package com.aicode.module.resource.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_resource_collect")
public class ResourceCollect {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long resourceId;
    private Long userId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
