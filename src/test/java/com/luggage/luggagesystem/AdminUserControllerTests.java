package com.luggage.luggagesystem;

import com.luggage.luggagesystem.controller.AuthController;
import com.luggage.luggagesystem.dto.RegisterRequest;
import com.luggage.luggagesystem.dto.UserResponse;
import com.luggage.luggagesystem.entity.SysUser;
import com.luggage.luggagesystem.enums.UserRole;
import com.luggage.luggagesystem.enums.UserStatus;
import com.luggage.luggagesystem.mapper.SysUserMapper;
import com.luggage.luggagesystem.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminUserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void administratorCanManageUserStatus()
            throws Exception {

        String username =
                "managed_" + System.nanoTime();

        String password = "test123456";

        // 创建普通用户
        RegisterRequest registerRequest =
                new RegisterRequest();

        registerRequest.setUsername(username);
        registerRequest.setPassword(password);
        registerRequest.setNickname("被管理用户");

        UserResponse normalUser =
                userService.register(registerRequest);

        // 创建管理员
        SysUser administrator =
                createAdministrator();

        MockHttpSession adminSession =
                createAdminSession(
                        administrator.getId()
                );

        // 管理员查询用户列表
        mockMvc.perform(
                        get("/api/admin/users")
                                .session(adminSession))
                .andExpect(status().isOk());

        // 管理员查询指定用户
        mockMvc.perform(
                        get(
                                "/api/admin/users/{userId}",
                                normalUser.getId()
                        ).session(adminSession))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(normalUser.getId())
                )
                .andExpect(
                        jsonPath("$.username")
                                .value(username)
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("NORMAL")
                )
                .andExpect(
                        jsonPath("$.passwordHash")
                                .doesNotExist()
                );

        // 管理员禁用普通用户
        mockMvc.perform(
                        put(
                                "/api/admin/users/{userId}/status",
                                normalUser.getId()
                        )
                                .session(adminSession)
                                .param(
                                        "status",
                                        "DISABLED"
                                ))
                .andExpect(status().isOk());

        // 确认用户已禁用
        mockMvc.perform(
                        get(
                                "/api/admin/users/{userId}",
                                normalUser.getId()
                        ).session(adminSession))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("DISABLED")
                );

        String loginBody = """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, password);

        // 被禁用的用户不能登录
        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(loginBody))
                .andExpect(status().isForbidden())
                .andExpect(
                        jsonPath("$.message")
                                .value("该账号已被停用")
                );

        // 管理员恢复普通用户
        mockMvc.perform(
                        put(
                                "/api/admin/users/{userId}/status",
                                normalUser.getId()
                        )
                                .session(adminSession)
                                .param(
                                        "status",
                                        "NORMAL"
                                ))
                .andExpect(status().isOk());

        // 恢复后的用户可以登录
        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.code")
                                .value(200)
                )
                .andExpect(
                        jsonPath("$.data.username")
                                .value(username)
                );

        // 管理员不能禁用当前登录的自己
        mockMvc.perform(
                        put(
                                "/api/admin/users/{userId}/status",
                                administrator.getId()
                        )
                                .session(adminSession)
                                .param(
                                        "status",
                                        "DISABLED"
                                ))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "管理员不能禁用当前登录账号"
                                )
                );

        // 查询不存在的用户
        mockMvc.perform(
                        get(
                                "/api/admin/users/{userId}",
                                999999999L
                        ).session(adminSession))
                .andExpect(status().isNotFound());
    }

    private SysUser createAdministrator() {

        SysUser administrator =
                new SysUser();

        administrator.setUsername(
                "admin_" + System.nanoTime()
        );

        administrator.setPasswordHash(
                passwordEncoder.encode(
                        "admin123456"
                )
        );

        administrator.setNickname("测试管理员");
        administrator.setRole(UserRole.ADMIN);
        administrator.setStatus(UserStatus.NORMAL);

        sysUserMapper.insert(administrator);

        return administrator;
    }

    private MockHttpSession createAdminSession(
            Long administratorId) {

        MockHttpSession session =
                new MockHttpSession();

        session.setAttribute(
                AuthController.LOGIN_USER_ID,
                administratorId
        );

        session.setAttribute(
                AuthController.LOGIN_USER_ROLE,
                UserRole.ADMIN.name()
        );

        return session;
    }
}
