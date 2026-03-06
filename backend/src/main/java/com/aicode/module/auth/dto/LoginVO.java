package com.aicode.module.auth.dto;

import lombok.Data;

@Data
public class LoginVO {
    private String accessToken;
    private String tokenType = "Bearer";
    private Long expiresIn; // seconds
    private Long userId;
    private String nickname;
    private Integer points;

    public LoginVO(String accessToken, Long expiresIn, Long userId, String nickname, Integer points) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.userId = userId;
        this.nickname = nickname;
        this.points = points;
    }
}
