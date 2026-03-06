package com.aicode.module.user.service.impl;

import com.aicode.common.exception.BizException;
import com.aicode.module.user.entity.User;
import com.aicode.module.user.mapper.UserMapper;
import com.aicode.module.user.service.UserService;
import com.aicode.module.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public UserVO getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BizException(404, "用户不存在");

        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setEmail(user.getEmail());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setPoints(user.getPoints());
        vo.setInviteCode(user.getInviteCode());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}
