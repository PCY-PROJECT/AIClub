package com.aicode.module.news.dto;

import lombok.Data;

/** 用户投稿资讯（status=1，待审核） */
@Data
public class SubmitNewsRequest {
    private String title;
    private String summary;
    private String cover;
    private String sourceUrl;
    private String tags;
    /** 1产品发布 2行业动态 3技术突破 4使用技巧 5政策监管 */
    private Integer category;
}
