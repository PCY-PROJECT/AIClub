package com.aicode.module.admin.dto;

import lombok.Data;

@Data
public class AdminAdjustPointsRequest {
    private Long userId;
    private Integer delta;    // 正数增加，负数减少
    private String remark;
}
