package com.aicode.module.auth.controller;

import com.aicode.common.result.R;
import com.aicode.module.auth.dto.*;
import com.aicode.module.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 获取图形验证码 */
    @GetMapping("/captcha")
    public R<CaptchaVO> captcha() {
        return R.ok(authService.getCaptcha());
    }

    /** 发送邮箱验证码 */
    @PostMapping("/send-email-code")
    public R<Void> sendEmailCode(@Valid @RequestBody SendEmailCodeRequest request,
                                  HttpServletRequest servletRequest) {
        authService.sendEmailCode(request, getClientIp(servletRequest));
        return R.ok();
    }

    /** 邮箱注册 */
    @PostMapping("/register")
    public R<Void> register(@Valid @RequestBody RegisterRequest request,
                             HttpServletRequest servletRequest) {
        authService.register(request, getClientIp(servletRequest));
        return R.ok();
    }

    /** 邮箱登录 */
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return R.ok(authService.login(request));
    }

    /** 登出（前端删除 token 即可，此接口兼容性保留） */
    @PostMapping("/logout")
    public R<Void> logout() {
        return R.ok();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isBlank()) ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
        if (ip != null && ip.contains(",")) ip = ip.split(",")[0].trim();
        return ip;
    }
}
