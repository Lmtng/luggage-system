package com.luggage.luggagesystem;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 生成 admin123 的加密密码
        String adminPassword = encoder.encode("admin123");
        System.out.println("admin123 加密后: " + adminPassword);
        System.out.println("长度: " + adminPassword.length());

        // 验证 admin123 是否能匹配
        boolean matches = encoder.matches("admin123", adminPassword);
        System.out.println("验证 admin123: " + matches);

        // 生成 test123 的加密密码
        String testPassword = encoder.encode("test123");
        System.out.println("test123 加密后: " + testPassword);
    }
}