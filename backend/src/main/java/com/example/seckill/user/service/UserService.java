package com.example.seckill.user.service;

import com.example.seckill.user.dto.LoginRequest;
import com.example.seckill.user.dto.RegisterRequest;
import com.example.seckill.user.dto.UpdatePasswordRequest;
import com.example.seckill.user.dto.UpdateProfileRequest;
import com.example.seckill.user.entity.User;

public interface UserService {

    /**
     * 用户注册
     *
     * @param req 注册请求（username, password, email）
     * @return 注册成功的用户实体
     */
    User register(RegisterRequest req);

    /**
     * 用户登录
     *
     * @param req 登录请求（username, password）
     * @return JWT token 字符串
     */
    String login(LoginRequest req);

    /**
     * 根据 ID 获取用户信息
     *
     * @param id 用户 ID
     * @return 用户实体
     */
    User getById(Long id);

    /**
     * 更新当前用户基础资料
     *
     * @param id 用户 ID
     * @param req 更新请求
     * @return 更新后的用户实体
     */
    User updateProfile(Long id, UpdateProfileRequest req);

    /**
     * 修改当前用户密码
     *
     * @param id 用户 ID
     * @param req 修改密码请求
     */
    void updatePassword(Long id, UpdatePasswordRequest req);
}
