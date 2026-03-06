package com.aicode.module.site.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_site")
public class Site {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String url;
    private String logo;
    private String category;
    private String tags;
    /** 状态：1待审核 2已发布 3已拒绝 */
    private Integer status;
    private Long submitUserId;
    private Integer viewCount;
    private Integer sort;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
