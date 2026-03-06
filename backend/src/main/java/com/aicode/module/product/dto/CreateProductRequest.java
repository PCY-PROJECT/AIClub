package com.aicode.module.product.dto;

import lombok.Data;

@Data
public class CreateProductRequest {
    private String name;
    private String description;
    private String officialUrl;
    private String logo;
    /** 1对话 2绘图 3代码 4视频 5音乐 9其他 */
    private Integer category;
    private Integer sort;
}
