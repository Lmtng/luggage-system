package com.luggage.luggagesystem.controller;

import com.luggage.luggagesystem.common.Result;
import com.luggage.luggagesystem.dto.LoginRequest;
import com.luggage.luggagesystem.dto.RegisterRequest;
import com.luggage.luggagesystem.dto.UserResponse;
import com.luggage.luggagesystem.enums.UserStatus;
import com.luggage.luggagesystem.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public static final String LOGIN_USER_ID = "LOGIN_USER_ID";
    public static final String LOGIN_USER_ROLE = "LOGIN_USER_ROLE";

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户注册。
     */
    @PostMapping("/register")
    public ResponseEntity<Result<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        UserResponse user = userService.register(request);
        Result<UserResponse> result =
                Result.success("注册成功", user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(result);
    }

    /**
     * 用户登录。
     */
    @PostMapping("/login")
    public ResponseEntity<Result<UserResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest) {

        UserResponse user = userService.login(request);

        HttpSession session = servletRequest.getSession(true);
        session.setAttribute(LOGIN_USER_ID, user.getId());
        session.setAttribute(LOGIN_USER_ROLE, user.getRole().name());

        Result<UserResponse> result =
                Result.success("登录成功", user);

        return ResponseEntity.ok(result);
    }

    /**
     * 获取当前登录用户。
     */
    @GetMapping("/me")
    public ResponseEntity<Result<UserResponse>> getCurrentUser(
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return unauthorized();
        }

        Object userIdValue =
                session.getAttribute(LOGIN_USER_ID);

        if (!(userIdValue instanceof Long userId)) {
            return unauthorized();
        }

        UserResponse user = userService.getUserById(userId);

        if (user == null) {
            session.invalidate();
            return unauthorized();
        }

        if (user.getStatus() == UserStatus.DISABLED) {
            session.invalidate();

            Result<UserResponse> result =
                    Result.error(
                            HttpStatus.FORBIDDEN.value(),
                            "该账号已被停用"
                    );

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(result);
        }

        return ResponseEntity.ok(Result.success(user));
    }

    /**
     * 退出登录。
     */
    @PostMapping("/logout")
    public ResponseEntity<Result<Void>> logout(
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        Result<Void> result =
                Result.success("退出登录成功", null);

        return ResponseEntity.ok(result);
    }

    private ResponseEntity<Result<UserResponse>> unauthorized() {
        Result<UserResponse> result =
                Result.error(
                        HttpStatus.UNAUTHORIZED.value(),
                        "请先登录"
                );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(result);
    }
}
