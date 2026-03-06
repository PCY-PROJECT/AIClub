package com.aicode.module.news.dto;

import lombok.Data;

/** 编辑直接创建并发布资讯（status=2） */
@Data
public class CreateNewsRequest {
    private String title;
    private String summary;
    private String cover;
    private String sourceUrl;
    private String tags;
    /** 1产品发布 2行业动态 3技术突破 4使用技巧 5政策监管 */
    private Integer category;
}
