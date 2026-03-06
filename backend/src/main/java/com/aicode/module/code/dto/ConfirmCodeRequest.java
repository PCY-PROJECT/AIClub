package com.aicode.module.code.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConfirmCodeRequest {
    /** "valid" 或 "invalid" */
    @NotBlank(message = "确认结果不能为空")
    private String result;
}
