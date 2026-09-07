package com.luggage.luggagesystem.config;

import com.luggage.luggagesystem.interceptor.AdminInterceptor;
import com.luggage.luggagesystem.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final AdminInterceptor adminInterceptor;

    public WebConfig(
            AuthInterceptor authInterceptor,
            AdminInterceptor adminInterceptor) {

        this.authInterceptor = authInterceptor;
        this.adminInterceptor = adminInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        /*
         * 普通登录验证：
         * 默认保护所有 /api 接口，
         * 注册、登录和空闲柜格查询接口除外。
         */
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/register",
                        "/api/auth/login",
                        "/api/locker-cells/available",
                        "/error"
                )
                .order(1);

        /*
         * 管理员权限验证：
         * 访问 /api/admin/** 时，
         * 先验证登录，再验证管理员角色。
         */
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/api/admin/**")
                .order(2);
    }
}