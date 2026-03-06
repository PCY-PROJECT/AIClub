package com.aicode.module.user.controller;

import com.aicode.common.result.R;
import com.aicode.common.security.SecurityUtil;
import com.aicode.module.user.service.UserService;
import com.aicode.module.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 获取当前登录用户信息（含积分） */
    @GetMapping("/me")
    public R<UserVO> me() {
        return R.ok(userService.getCurrentUser(SecurityUtil.getCurrentUserId()));
    }
}
