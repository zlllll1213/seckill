package com.example.seckill.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.seckill.common.BusinessException;
import com.example.seckill.common.ErrorCode;
import com.example.seckill.config.JwtUtils;
import com.example.seckill.user.dto.LoginRequest;
import com.example.seckill.user.dto.RegisterRequest;
import com.example.seckill.user.dto.UpdatePasswordRequest;
import com.example.seckill.user.dto.UpdateProfileRequest;
import com.example.seckill.user.entity.User;
import com.example.seckill.user.mapper.UserMapper;
import com.example.seckill.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public User register(RegisterRequest req) {
        // 检查用户名唯一性
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, req.getUsername())
        );
        if (count != null && count > 0) {
            throw new BusinessException(ErrorCode.USERNAME_DUPLICATE);
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setEmail(req.getEmail());
        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);

        // 不返回密码
        user.setPassword(null);
        return user;
    }

    @Override
    public String login(LoginRequest req) {
        // 查询用户（含密码字段）
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, req.getUsername())
                        .select(User::getId, User::getUsername, User::getPassword, User::getRole)
        );
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }
        return jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    public User getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    @Override
    public User updateProfile(Long id, UpdateProfileRequest req) {
        User existing = userMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        User user = new User();
        user.setId(id);
        user.setEmail(req.getEmail());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        User updated = userMapper.selectById(id);
        updated.setPassword(null);
        return updated;
    }

    @Override
    public void updatePassword(Long id, UpdatePasswordRequest req) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getId, id)
                        .select(User::getId, User::getPassword)
        );
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        User update = new User();
        update.setId(id);
        update.setPassword(passwordEncoder.encode(req.getNewPassword()));
        update.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(update);
    }
}
