package com.aicode.module.code.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CodeItemVO {
    private Long id;
    private Long productId;
    private String productName;
    private Long providerId;
    private String providerNickname;
    private String providerAvatar;
    private String codePreview;
    /**
     * status 语义：
     * 1=池中可用 2=已分配 3=已确认有效 4=已确认无效 5=待审核 6=审核拒绝
     */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime claimTime;
    private LocalDateTime confirmDeadline;
    private Integer confirmResult;
}
