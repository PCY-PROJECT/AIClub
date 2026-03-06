package com.aicode.module.point.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_point_record")
public class PointRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer delta;    // 积分变化（正增负减）
    private Integer balance;  // 变动后余额快照
    private Integer bizType;  // 1注册 2签到 3查看邀请码 4邀请码被查看 5邀请用户 6任务
    private String bizId;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
