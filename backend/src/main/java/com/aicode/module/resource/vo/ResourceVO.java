package com.aicode.module.resource.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResourceVO {
    private Long id;
    private String title;
    private String summary;
    private String sourceUrl;
    private String cover;
    private String author;
    private Integer category;
    private String categoryName;
    private Integer difficulty;
    private String difficultyName;
    private String tags;
    private Integer status;
    private Integer viewCount;
    private Integer collectCount;
    private Long submitUserId;
    private String submitUserNickname;
    private boolean collected;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
}
