package com.aicode.module.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String logo;
    private String description;
    private String officialUrl;
    private Integer category;     // 1对话 2绘图 3代码 4视频 5音乐 9其他
    private Integer productGroup;  // 1=OpenClaw生态 2=AI应用 3=其他
    private Integer status;      // 1上线 2下线
    private Integer sort;
    private Integer codeCount;   // 当前可用邀请码数量（冗余）
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
