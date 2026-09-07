package com.luggage.luggagesystem.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luggage.luggagesystem.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;

    public AdminInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws IOException {

        Object userRole =
                request.getAttribute(
                        AuthInterceptor.USER_ROLE_ATTR
                );

        if (userRole instanceof String role
                && "ADMIN".equalsIgnoreCase(role)) {

            return true;
        }

        writeForbiddenResponse(response);
        return false;
    }

    private void writeForbiddenResponse(
            HttpServletResponse response) throws IOException {

        HttpStatus status = HttpStatus.FORBIDDEN;

        ApiErrorResponse errorResponse =
                new ApiErrorResponse(
                        status.value(),
                        "需要管理员权限",
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