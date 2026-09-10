package com.luggage.luggagesystem;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.luggage.luggagesystem.dto.LoginRequest;
import com.luggage.luggagesystem.dto.RegisterRequest;
import com.luggage.luggagesystem.dto.UserResponse;
import com.luggage.luggagesystem.entity.SysUser;
import com.luggage.luggagesystem.enums.UserRole;
import com.luggage.luggagesystem.enums.UserStatus;
import com.luggage.luggagesystem.mapper.SysUserMapper;
import com.luggage.luggagesystem.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class UserServiceTests {

    @Autowired
    private UserService userService;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registerAndLoginWorks() {
        String username = "user_" + System.nanoTime();
        String rawPassword = "test123456";

        RegisterRequest registerRequest =
                new RegisterRequest();

        registerRequest.setUsername(username);
        registerRequest.setPassword(rawPassword);
        registerRequest.setNickname("注册测试用户");

        // 注册用户
        UserResponse registered =
                userService.register(registerRequest);

        assertNotNull(registered.getId());
        assertEquals(username, registered.getUsername());
        assertEquals("注册测试用户", registered.getNickname());
        assertEquals(UserRole.USER, registered.getRole());
        assertEquals(UserStatus.NORMAL, registered.getStatus());

        // 检查数据库中的密码不是明文
        SysUser storedUser = sysUserMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery()
                        .eq(SysUser::getUsername, username)
        );

        assertNotNull(storedUser);
        assertNotEquals(
                rawPassword,
                storedUser.getPasswordHash()
        );
        assertTrue(
                passwordEncoder.matches(
                        rawPassword,
                        storedUser.getPasswordHash()
                )
        );

        // 使用正确密码登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(rawPassword);

        UserResponse loggedIn =
                userService.login(loginRequest);

        assertEquals(registered.getId(), loggedIn.getId());

        // 使用错误密码登录
        LoginRequest wrongPasswordRequest =
                new LoginRequest();

        wrongPasswordRequest.setUsername(username);
        wrongPasswordRequest.setPassword("wrong123");

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.login(wrongPasswordRequest)
        );

        // 重复注册相同用户名
        assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(registerRequest)
        );

        // 禁用账号后不能登录
        storedUser.setStatus(UserStatus.DISABLED);
        sysUserMapper.updateById(storedUser);

        IllegalStateException disabledException = assertThrows(
                IllegalStateException.class,
                () -> userService.login(loginRequest)
        );

        assertEquals(
                "该账号已被停用",
                disabledException.getMessage()
        );
    }
}
