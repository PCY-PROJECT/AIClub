package com.aicode.module.resource.dto;

import lombok.Data;

@Data
public class CreateResourceRequest {
    private String title;
    private String summary;
    private String sourceUrl;   // 必填
    private String cover;
    private String author;
    private Integer category;   // 1-6
    private Integer difficulty; // 1-3
    private String tags;
}
