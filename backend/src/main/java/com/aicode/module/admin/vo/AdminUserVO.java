package com.aicode.module.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserVO {
    private Long id;
    private String email;
    private String nickname;
    private Integer points;
    private Integer status;
    private Integer role;
    private Integer userNo;
    private String inviteCode;
    private String regIp;
    private LocalDateTime createTime;
}
