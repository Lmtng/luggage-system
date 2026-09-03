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

import java.time.LocalDateTime;

@Component
public class LoginAuthInterceptor
        implements HandlerInterceptor {

    private final ObjectMapper objectMapper;

    public LoginAuthInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        HttpSession session =
                request.getSession(false);

        if (session == null
                || session.getAttribute(
                AuthController.LOGIN_USER_ID
        ) == null) {

            ApiErrorResponse errorResponse =
                    new ApiErrorResponse(
                            HttpStatus.UNAUTHORIZED.value(),
                            "请先登录",
                            LocalDateTime.now()
                    );

            response.setStatus(
                    HttpStatus.UNAUTHORIZED.value()
            );
            response.setCharacterEncoding("UTF-8");
            response.setContentType(
                    "application/json;charset=UTF-8"
            );

            response.getWriter().write(
                    objectMapper.writeValueAsString(
                            errorResponse
                    )
            );

            return false;
        }

        return true;
    }
}