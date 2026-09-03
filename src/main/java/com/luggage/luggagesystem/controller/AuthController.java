package com.luggage.luggagesystem.controller;

import com.luggage.luggagesystem.dto.ApiErrorResponse;
import com.luggage.luggagesystem.dto.LoginRequest;
import com.luggage.luggagesystem.dto.RegisterRequest;
import com.luggage.luggagesystem.dto.UserResponse;
import com.luggage.luggagesystem.enums.UserStatus;
import com.luggage.luggagesystem.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public static final String LOGIN_USER_ID =
            "LOGIN_USER_ID";

    public static final String LOGIN_USER_ROLE =
            "LOGIN_USER_ROLE";

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @RequestBody RegisterRequest request) {

        UserResponse registeredUser =
                userService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(
            @RequestBody LoginRequest request,
            HttpSession session) {

        UserResponse user = userService.login(request);

        session.setAttribute(
                LOGIN_USER_ID,
                user.getId()
        );

        session.setAttribute(
                LOGIN_USER_ROLE,
                user.getRole().name()
        );

        return ResponseEntity.ok(user);
    }

    /**
     * 查询当前登录用户。
     */
    @GetMapping("/me")
    public ResponseEntity<?> currentUser(
            HttpServletRequest request) {

        HttpSession session =
                request.getSession(false);

        if (session == null) {
            return unauthorizedResponse();
        }

        Object userIdValue =
                session.getAttribute(LOGIN_USER_ID);

        if (!(userIdValue instanceof Long userId)) {
            return unauthorizedResponse();
        }

        UserResponse user =
                userService.getUserById(userId);

        if (user == null) {
            session.invalidate();
            return unauthorizedResponse();
        }

        if (user.getStatus() == UserStatus.DISABLED) {
            session.invalidate();

            ApiErrorResponse error =
                    new ApiErrorResponse(
                            HttpStatus.FORBIDDEN.value(),
                            "账号已被禁用",
                            LocalDateTime.now()
                    );

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(error);
        }

        return ResponseEntity.ok(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            HttpServletRequest request) {

        HttpSession session =
                request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.ok("退出登录成功");
    }

    private ResponseEntity<ApiErrorResponse>
    unauthorizedResponse() {

        ApiErrorResponse error =
                new ApiErrorResponse(
                        HttpStatus.UNAUTHORIZED.value(),
                        "请先登录",
                        LocalDateTime.now()
                );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(error);
    }
}