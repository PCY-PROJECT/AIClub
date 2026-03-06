package com.aicode.module.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminCodeVO {
    private Long id;
    private Long productId;
    private String productName;
    private Long providerId;
    private String providerNickname;
    private String codePreview;
    /**
     * 1=可用 2=已分配 3=有效 4=无效 5=待审核 6=已拒绝
     */
    private Integer status;
    private LocalDateTime createTime;
    // 获取者信息
    private Long claimantId;
    private String claimantNickname;
    private LocalDateTime claimTime;
    private LocalDateTime confirmDeadline;
    private Integer confirmResult;
    private LocalDateTime reviewTime;
}
