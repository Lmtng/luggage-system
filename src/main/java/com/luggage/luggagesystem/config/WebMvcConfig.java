package com.luggage.luggagesystem.config;

import com.luggage.luggagesystem.interceptor.AdminAuthInterceptor;
import com.luggage.luggagesystem.interceptor.LoginAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AdminAuthInterceptor adminAuthInterceptor;
    private final LoginAuthInterceptor loginAuthInterceptor;

    public WebMvcConfig(
            AdminAuthInterceptor adminAuthInterceptor,
            LoginAuthInterceptor loginAuthInterceptor) {

        this.adminAuthInterceptor = adminAuthInterceptor;
        this.loginAuthInterceptor = loginAuthInterceptor;
    }

    @Override
    public void addInterceptors(
            InterceptorRegistry registry) {

        // 管理员接口：必须登录且角色必须是ADMIN
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/api/admin/**");

        // 柜格占用和释放：只要求用户已经登录
        registry.addInterceptor(loginAuthInterceptor)
                .addPathPatterns(
                        "/api/locker-cells/*/occupy",
                        "/api/locker-cells/*/release"
                );
    }
}