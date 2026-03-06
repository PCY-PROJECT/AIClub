package com.aicode.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String email;
    private String password;
    private String nickname;
    private String avatar;
    private Integer points;
    private Integer status;       // 1正常 2封禁
    private String inviteCode;    // 用户推广邀请码
    private Long inviterId;
    private String regIp;
    /** 角色：1普通用户 2编辑员 9超级管理员 */
    private Integer role;
    /** 注册序号（早期用户身份依据） */
    private Integer userNo;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
