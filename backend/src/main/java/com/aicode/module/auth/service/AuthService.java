package com.aicode.module.auth.service;

import com.aicode.module.auth.dto.*;

public interface AuthService {
    CaptchaVO getCaptcha();
    void sendEmailCode(SendEmailCodeRequest request, String clientIp);
    void register(RegisterRequest request, String clientIp);
    LoginVO login(LoginRequest request);
}
