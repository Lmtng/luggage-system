package com.luggage.luggagesystem.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luggage.luggagesystem.controller.AuthController;
import com.luggage.luggagesystem.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String USER_ID_ATTR = "userId";
    public static final String USER_ROLE_ATTR = "userRole";

    private final ObjectMapper objectMapper;

    public AuthInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null) {
            writeUnauthorizedResponse(response);
            return false;
        }

        Object userId =
                session.getAttribute(AuthController.LOGIN_USER_ID);

        Object userRole =
                session.getAttribute(AuthController.LOGIN_USER_ROLE);

        if (!(userId instanceof Long)
                || !(userRole instanceof String)) {

            session.invalidate();
            writeUnauthorizedResponse(response);
            return false;
        }

        // 供成员B的AuthContext和订单模块读取
        request.setAttribute(USER_ID_ATTR, userId);
        request.setAttribute(USER_ROLE_ATTR, userRole);

        return true;
    }

    private void writeUnauthorizedResponse(
            HttpServletResponse response) throws IOException {

        HttpStatus status = HttpStatus.UNAUTHORIZED;

        ApiErrorResponse errorResponse =
                new ApiErrorResponse(
                        status.value(),
                        "请先登录",
                        LocalDateTime.now()
                );

        response.setStatus(status.value());
        response.setContentType(
                "application/json;charset=UTF-8"
        );

        objectMapper.writeValue(
                response.getWriter(),
                errorResponse
        );
    }
}