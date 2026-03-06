package com.aicode.module.code.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_invite_code")
public class InviteCode {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private Long providerId;
    /** 兼容旧字段，不再使用 */
    private Long parentCodeId;
    private String codeEncrypted;
    private String codePreview;
    /**
     * status 语义：
     * 1=池中可用（审核通过）
     * 2=已分配（获取者已获取，待12h确认）
     * 3=已确认有效
     * 4=已确认无效
     * 5=待审核（刚提交）
     * 6=审核拒绝
     */
    private Integer status;
    /** 兼容旧字段（已废弃） */
    private Long viewerId;
    /** 兼容旧字段（已废弃） */
    private LocalDateTime viewTime;
    /** 获取者用户ID */
    private Long claimantId;
    /** 获取时间 */
    private LocalDateTime claimTime;
    /** 确认截止时间（获取后12小时） */
    private LocalDateTime confirmDeadline;
    /** 实际确认时间 */
    private LocalDateTime confirmTime;
    /** 确认结果 1=有效 2=无效 */
    private Integer confirmResult;
    /** 审核管理员ID */
    private Long reviewerId;
    /** 审核时间 */
    private LocalDateTime reviewTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
