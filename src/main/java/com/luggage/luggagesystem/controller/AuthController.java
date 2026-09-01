package com.luggage.luggagesystem.controller;

import com.luggage.luggagesystem.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 *
 * 功能：
 * 1. 用户注册
 * 2. 用户登录
 * 3. 获取当前用户信息
 *
 * 接口路径：
 * - POST /api/auth/register  注册
 * - POST /api/auth/login     登录
 * - GET  /api/users/me       获取当前用户
 *
 * @author 成员B
 * @date 2026-09-01
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * 用户注册
     *
     * 请求体：
     * {
     *   "username": "testuser",
     *   "password": "123456",
     *   "nickname": "测试用户"
     * }
     *
     * 响应：
     * {
     *   "code": 200,
     *   "message": "注册成功",
     *   "data": {
     *     "userId": 1,
     *     "username": "testuser",
     *     "role": "USER"
     *   }
     * }
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        // TODO: 实现注册逻辑
        // 1. 校验用户名是否已存在
        // 2. 加密密码
        // 3. 保存用户

        log.info("注册请求: username={}", request.get("username"));

        // 模拟注册成功
        Map<String, Object> data = new HashMap<>();
        data.put("userId", 1L);
        data.put("username", request.get("username"));
        data.put("role", "USER");

        return Result.success("注册成功", data);
    }

    /**
     * 用户登录
     *
     * 请求体：
     * {
     *   "username": "testuser",
     *   "password": "123456"
     * }
     *
     * 响应：
     * {
     *   "code": 200,
     *   "message": "登录成功",
     *   "data": {
     *     "userId": 1,
     *     "username": "testuser",
     *     "role": "USER",
     *     "token": "模拟token"
     *   }
     * }
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        log.info("登录请求: username={}", request.get("username"));

        // TODO: 实现登录逻辑
        // 1. 查询用户
        // 2. 校验密码
        // 3. 生成登录凭证

        Map<String, Object> data = new HashMap<>();
        data.put("userId", 1L);
        data.put("username", request.get("username"));
        data.put("role", "USER");
        data.put("token", "mock-token-123456");

        return Result.success("登录成功", data);
    }

    /**
     * 获取当前用户信息
     *
     * 响应：
     * {
     *   "code": 200,
     *   "message": "操作成功",
     *   "data": {
     *     "userId": 1,
     *     "username": "testuser",
     *     "nickname": "测试用户",
     *     "role": "USER"
     *   }
     * }
     */
    @GetMapping("/users/me")
    public Result<Map<String, Object>> getCurrentUser() {
        // TODO: 从登录凭证获取当前用户

        Map<String, Object> data = new HashMap<>();
        data.put("userId", 1L);
        data.put("username", "testuser");
        data.put("nickname", "测试用户");
        data.put("role", "USER");

        return Result.success(data);
    }
}