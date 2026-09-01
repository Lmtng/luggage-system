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
 * 登录拦截器
 *
 * 功能：
 * 1. 检查请求是否携带有效的登录凭证（Token）
 * 2. 从 Token 中解析用户ID和角色，存入请求上下文
 * 3. 放行公开接口（注册、登录）
 *
 * 拦截规则：
 * - /api/auth/register  → 放行（公开）
 * - /api/auth/login     → 放行（公开）
 * - 其他 /api/** 接口   → 需要登录
 *
 * @author 成员B
 * @date 2026-09-01
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    /**
     * 请求头中存放 Token 的键名
     */
    private static final String TOKEN_HEADER = "Authorization";

    /**
     * Token 前缀
     */
    private static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 用户ID在请求中的属性名
     */
    public static final String USER_ID_ATTR = "userId";

    /**
     * 用户角色在请求中的属性名
     */
    public static final String USER_ROLE_ATTR = "userRole";

    /**
     * JSON 序列化工具
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 前置拦截：在请求到达 Controller 之前执行
     *
     * @return true 放行，false 拦截
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        // 1. 放行公开接口（注册、登录）
        if (path.equals("/api/auth/register") || path.equals("/api/auth/login")) {
            log.debug("放行公开接口: {}", path);
            return true;
        }

        // 2. 获取 Token
        String token = getTokenFromRequest(request);

        // 3. 验证 Token
        if (token == null || !validateToken(token)) {
            log.warn("未登录或 Token 无效: {}", path);
            // 返回 401 未授权
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");

            Result<Void> result = Result.error(ResultCode.UNAUTHORIZED);
            String json = objectMapper.writeValueAsString(result);

            try (PrintWriter writer = response.getWriter()) {
                writer.write(json);
                writer.flush();
            }
            return false;
        }

        // 4. 从 Token 解析用户信息并存入请求上下文
        Long userId = getUserIdFromToken(token);
        String role = getRoleFromToken(token);

        request.setAttribute(USER_ID_ATTR, userId);
        request.setAttribute(USER_ROLE_ATTR, role);

        log.debug("登录验证通过: userId={}, role={}, path={}", userId, role, path);
        return true;
    }

    /**
     * 从请求头中获取 Token
     *
     * 格式：Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(TOKEN_HEADER);
        if (authHeader != null && authHeader.startsWith(TOKEN_PREFIX)) {
            return authHeader.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    /**
     * 验证 Token 是否有效
     *
     * TODO: 实际项目中需要 JWT 或 Redis 验证
     * 目前使用模拟验证
     */
    private boolean validateToken(String token) {
        // TODO: 实现真正的 Token 验证
        // 1. JWT 验证签名和过期时间
        // 2. 或查询 Redis 中是否存在该 Token

        // 临时：非空且长度大于10就认为有效
        return token != null && token.length() > 10;
    }

    /**
     * 从 Token 中获取用户ID
     *
     * TODO: 实际项目中从 JWT 或 Redis 中解析
     */
    private Long getUserIdFromToken(String token) {
        // TODO: 从 JWT 中解析用户ID
        // 临时：返回模拟ID
        return 100L;
    }

    /**
     * 从 Token 中获取用户角色
     *
     * TODO: 实际项目中从 JWT 或 Redis 中解析
     */
    private String getRoleFromToken(String token) {
        // TODO: 从 JWT 中解析角色
        // 临时：返回默认角色
        return "USER";
    }
}