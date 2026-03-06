package com.aicode.module.news.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewsVO {
    private Long id;
    private String title;
    private String summary;
    private String cover;
    private String sourceUrl;
    private String tags;
    private Integer category;
    private String categoryName;
    private Long authorId;
    private String authorNickname;
    private Integer likeCount;
    private Integer viewCount;
    private Boolean liked;            // 当前用户是否已点赞（未登录时为 null）
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
    /** 1待审核 2已发布 3已拒绝 */
    private Integer status;
}
