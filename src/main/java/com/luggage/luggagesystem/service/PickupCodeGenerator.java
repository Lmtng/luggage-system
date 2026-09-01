package com.luggage.luggagesystem.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

/**
 * 取件码生成器
 *
 * 功能说明：
 * 1. 生成6位随机数字取件码（纯数字，方便用户输入）
 * 2. 使用BCrypt加密存储（不保存明文）
 * 3. 保证生成的取件码不重复（简单防冲突）
 *
 * 设计思想：
 * - 取件码是用户取件的凭证，必须安全存储
 * - 数据库只保存BCrypt摘要，即使泄露也无法反推出取件码
 * - 校验时比较取件码的摘要是否匹配
 *
 * @author 成员B
 * @date 2026-09-01
 */
@Component
public class PickupCodeGenerator {

    /**
     * 取件码长度（6位数字）
     */
    private static final int CODE_LENGTH = 6;

    /**
     * 密码加密器（BCrypt）
     */
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 安全随机数生成器
     */
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * 用于防止短时间内生成重复取件码的缓存
     * 实际项目中可用Redis，这里用简单内存缓存
     */
    private final Set<String> recentCodes = new HashSet<>();

    /**
     * 生成6位数字取件码
     *
     * @return 6位数字取件码（如：837291）
     */
    public String generateCode() {
        // 生成6位数字：100000 ~ 999999
        int code = secureRandom.nextInt(900000) + 100000;
        String codeStr = String.valueOf(code);

        // 简单防冲突：如果最近生成过，重新生成
        // 100万种组合，冲突概率很低，但加上保险
        synchronized (recentCodes) {
            if (recentCodes.contains(codeStr)) {
                // 递归重新生成
                return generateCode();
            }
            // 缓存最近生成的取件码（最多1000个，防止内存溢出）
            if (recentCodes.size() > 1000) {
                recentCodes.clear();
            }
            recentCodes.add(codeStr);
        }

        return codeStr;
    }

    /**
     * 对取件码进行加密（生成摘要）
     *
     * @param pickupCode 明文取件码
     * @return BCrypt加密后的摘要
     */
    public String encryptCode(String pickupCode) {
        return passwordEncoder.encode(pickupCode);
    }

    /**
     * 验证取件码是否正确
     *
     * @param rawPickupCode 用户输入的明文取件码
     * @param encryptedPickupCode 数据库中存储的加密摘要
     * @return true=匹配，false=不匹配
     */
    public boolean verifyCode(String rawPickupCode, String encryptedPickupCode) {
        if (rawPickupCode == null || encryptedPickupCode == null) {
            return false;
        }
        return passwordEncoder.matches(rawPickupCode, encryptedPickupCode);
    }
}