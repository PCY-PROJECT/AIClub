package com.aicode.module.news.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_news_like")
public class NewsLike {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long newsId;
    private Long userId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
