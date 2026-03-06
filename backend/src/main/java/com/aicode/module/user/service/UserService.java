package com.aicode.module.user.service;

import com.aicode.module.user.vo.UserVO;

public interface UserService {
    UserVO getCurrentUser(Long userId);
}
