package com.luggage.luggagesystem.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luggage.luggagesystem.common.Result;
import com.luggage.luggagesystem.common.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

/**
 * 管理员权限拦截器
 *
 * 功能：
 * 1. 检查当前用户是否为 ADMIN 角色
 * 2. 仅对 /api/admin/** 路径生效
 *
 * @author 成员B
 * @date 2026-09-01
 */
@Slf4j
@Component
public class AdminInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 从请求中获取用户信息（由 AuthInterceptor 设置）
        String role = (String) request.getAttribute(AuthInterceptor.USER_ROLE_ATTR);
        Long userId = (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);

        // 2. 校验是否为管理员
        if (role == null || !"ADMIN".equals(role)) {
            log.warn("权限不足: userId={}, role={}, path={}", userId, role, request.getRequestURI());

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");

            Result<Void> result = Result.error(ResultCode.FORBIDDEN);
            String json = objectMapper.writeValueAsString(result);

            try (PrintWriter writer = response.getWriter()) {
                writer.write(json);
                writer.flush();
            }
            return false;
        }

        log.debug("管理员权限验证通过: userId={}", userId);
        return true;
    }
}