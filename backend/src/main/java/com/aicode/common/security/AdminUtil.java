package com.aicode.common.security;

import com.aicode.common.exception.BizException;
import com.aicode.module.user.entity.User;
import com.aicode.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminUtil {

    private final UserMapper userMapper;

    /** 要求管理员角色（role=9），否则抛 403 */
    public void requireAdmin(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getRole() == null || user.getRole() < 9) {
            throw new BizException(403, "无权限，需要管理员身份");
        }
    }

    /** 要求编辑员及以上角色（role>=2），否则抛 403 */
    public void requireEditor(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getRole() == null || user.getRole() < 2) {
            throw new BizException(403, "无权限，需要编辑员及以上身份");
        }
    }
}
