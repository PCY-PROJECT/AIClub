package com.aicode.module.user.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String email;
    private String nickname;
    private String avatar;
    private Integer points;
    private String inviteCode;
    private LocalDateTime createTime;
}
