package com.luggage.luggagesystem.common;

import com.luggage.luggagesystem.interceptor.AuthInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 认证上下文工具类
 *
 * 功能：在 Controller 或 Service 中方便地获取当前登录用户信息
 *
 * 使用方式：
 * - Long userId = AuthContext.getCurrentUserId();
 * - String role = AuthContext.getCurrentRole();
 *
 * @author 成员B
 * @date 2026-09-01
 */
public class AuthContext {

    /**
     * 获取当前请求对象
     */
    private static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return attributes.getRequest();
    }

    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
    }

    /**
     * 获取当前登录用户角色
     */
    public static String getCurrentRole() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return (String) request.getAttribute(AuthInterceptor.USER_ROLE_ATTR);
    }

    /**
     * 判断当前用户是否为管理员
     */
    public static boolean isAdmin() {
        return "ADMIN".equals(getCurrentRole());
    }

    /**
     * 判断当前用户是否已登录
     */
    public static boolean isLogin() {
        return getCurrentUserId() != null;
    }
}