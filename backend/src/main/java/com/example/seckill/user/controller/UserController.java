package com.example.seckill.user.controller;

import com.example.seckill.common.Result;
import com.example.seckill.user.dto.LoginRequest;
import com.example.seckill.user.dto.RegisterRequest;
import com.example.seckill.user.dto.UpdatePasswordRequest;
import com.example.seckill.user.dto.UpdateProfileRequest;
import com.example.seckill.user.entity.User;
import com.example.seckill.user.service.UserService;
import com.example.seckill.config.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    /**
     * 用户登录，返回 JWT token 及用户名
     */
    @PostMapping("/login")
    public Result<Map<String, String>> login(@Valid @RequestBody LoginRequest req, HttpServletResponse response) {
        String token = userService.login(req);
        Claims claims = jwtUtils.parseToken(token);

        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) jwtUtils.getExpirationSeconds());
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);

        Map<String, String> data = new HashMap<>();
        data.put("username", claims.get("username", String.class));
        data.put("role", claims.get("role", String.class));
        return Result.success(data);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody RegisterRequest req) {
        User user = userService.register(req);
        return Result.success(user);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/me")
    public Result<User> me() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getById(userId);
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 更新当前登录用户基础资料
     */
    @PutMapping("/me")
    public Result<User> updateMe(@Valid @RequestBody UpdateProfileRequest req) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.updateProfile(userId, req);
        return Result.success(user);
    }

    /**
     * 修改当前登录用户密码
     */
    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest req) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.updatePassword(userId, req);
        return Result.success(null);
    }
}
