package com.aicode.module.news.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_news")
public class News {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String summary;       // 摘要，100字内
    private String cover;         // 封面图 URL
    private String sourceUrl;     // 来源链接
    private String tags;          // 标签，逗号分隔
    /** 分类：1产品发布 2行业动态 3技术突破 4使用技巧 5政策监管 */
    private Integer category;
    private Long authorId;        // NULL=编辑发布，非NULL=用户投稿
    /** 状态：1待审核 2已发布 3已拒绝 */
    private Integer status;
    private Integer likeCount;
    private Integer viewCount;
    private LocalDateTime publishTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
