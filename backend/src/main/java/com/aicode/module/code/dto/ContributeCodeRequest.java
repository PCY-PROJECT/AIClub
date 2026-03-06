package com.aicode.module.code.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ContributeCodeRequest {
    @NotNull(message = "产品ID不能为空")
    private Long productId;

    @NotBlank(message = "邀请码不能为空")
    private String code;
}
