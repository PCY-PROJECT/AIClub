package com.aicode.module.skill.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SkillVO {
    private Long id;
    private String title;
    private String prompt;
    private String applicable;
    private String category;
    private String tags;
    private Long authorId;
    private String authorNickname;
    private Integer likeCount;
    private Integer collectCount;
    private Integer useCount;
    private Boolean liked;       // 当前用户是否已点赞
    private Boolean collected;   // 当前用户是否已收藏
    private LocalDateTime createTime;
    private Integer status;
}
