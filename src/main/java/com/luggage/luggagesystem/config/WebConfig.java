package com.luggage.luggagesystem.config;

import com.luggage.luggagesystem.interceptor.AdminInterceptor;
import com.luggage.luggagesystem.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置类
 *
 * 功能：配置拦截器
 *
 * 拦截规则：
 * 1. AuthInterceptor：所有 /api/** 请求都需要登录（除了注册、登录）
 * 2. AdminInterceptor：所有 /api/admin/** 请求都需要管理员权限
 *
 * @author 成员B
 * @date 2026-09-01
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final AdminInterceptor adminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // ========== 1. 登录拦截器 ==========
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")                          // 拦截所有 /api/** 请求
                .excludePathPatterns(                                // 排除公开接口
                        "/api/auth/register",
                        "/api/auth/login"
                );

        // ========== 2. 管理员权限拦截器 ==========
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/api/admin/**");                   // 只拦截 /api/admin/**
    }
}