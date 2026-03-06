package com.aicode.module.code.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SubmitCodeRequest {
    @NotNull(message = "产品ID不能为空")
    private Long productId;

    @NotBlank(message = "邀请码内容不能为空")
    @Size(max = 256, message = "邀请码长度不能超过256位")
    private String code;

    /** 接龙来源邀请码 ID，首发时为 null */
    private Long parentCodeId;
}
