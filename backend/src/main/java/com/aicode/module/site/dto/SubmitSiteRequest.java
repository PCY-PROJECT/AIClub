package com.aicode.module.site.dto;

import lombok.Data;

@Data
public class SubmitSiteRequest {
    private String name;
    private String description;
    private String url;
    private String logo;
    private String category;
    private String tags;
}
