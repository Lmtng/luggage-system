package com.luggage.luggagesystem.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luggage.luggagesystem.controller.AuthController;
import com.luggage.luggagesystem.dto.ApiErrorResponse;
import com.luggage.luggagesystem.enums.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Component
public class AdminAuthInterceptor
        implements HandlerInterceptor {

    private final ObjectMapper objectMapper;

    public AdminAuthInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        HttpSession session = request.getSession(false);

        // 没有Session或Session中没有用户ID，表示尚未登录
        if (session == null
                || session.getAttribute(
                AuthController.LOGIN_USER_ID
        ) == null) {

            writeError(
                    response,
                    HttpStatus.UNAUTHORIZED,
                    "请先登录"
            );

            return false;
        }

        Object role = session.getAttribute(
                AuthController.LOGIN_USER_ROLE
        );

        // 已登录，但不是管理员
        if (!UserRole.ADMIN.name().equals(role)) {
            writeError(
                    response,
                    HttpStatus.FORBIDDEN,
                    "需要管理员权限"
            );

            return false;
        }

        return true;
    }

    private void writeError(
            HttpServletResponse response,
            HttpStatus status,
            String message) throws Exception {

        response.setStatus(status.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(
                "application/json;charset=UTF-8"
        );

        ApiErrorResponse errorResponse =
                new ApiErrorResponse(
                        status.value(),
                        message,
                        LocalDateTime.now()
                );

        response.getWriter().write(
                objectMapper.writeValueAsString(
                        errorResponse
                )
        );
    }
}