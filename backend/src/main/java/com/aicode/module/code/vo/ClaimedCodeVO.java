package com.aicode.module.code.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClaimedCodeVO {
    private Long codeId;
    private Long productId;
    private String productName;
    /** 明文邀请码（解密后） */
    private String codeText;
    /**
     * 2=已分配待确认 3=已确认有效 4=已确认无效
     */
    private Integer status;
    private LocalDateTime claimTime;
    private LocalDateTime confirmDeadline;
    private LocalDateTime confirmTime;
    /** 1=有效 2=无效 */
    private Integer confirmResult;
}
