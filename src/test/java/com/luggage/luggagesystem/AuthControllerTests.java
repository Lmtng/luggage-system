package com.luggage.luggagesystem;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luggage.luggagesystem.controller.AuthController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerLoginCurrentUserAndLogoutWorks()
            throws Exception {

        String username =
                "api_" + System.nanoTime();

        String password = "test123456";

        // 未登录时查询当前用户，应返回401
        mockMvc.perform(
                        get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.message")
                                .value("请先登录")
                );

        String registerBody = """
                {
                  "username": "%s",
                  "password": "%s",
                  "nickname": "接口测试用户"
                }
                """.formatted(username, password);

        // 注册
        String registerResponse = mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(registerBody))
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.username")
                                .value(username)
                )
                .andExpect(
                        jsonPath("$.nickname")
                                .value("接口测试用户")
                )
                .andExpect(
                        jsonPath("$.role")
                                .value("USER")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("NORMAL")
                )
                .andExpect(
                        jsonPath("$.passwordHash")
                                .doesNotExist()
                )
                .andReturn()
                .getResponse()
                .getContentAsString(
                        StandardCharsets.UTF_8
                );

        JsonNode registeredUser =
                objectMapper.readTree(registerResponse);

        long userId =
                registeredUser.get("id").asLong();

        String loginBody = """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, password);

        // 正确密码登录
        MvcResult loginResult = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(userId)
                )
                .andExpect(
                        jsonPath("$.username")
                                .value(username)
                )
                .andReturn();

        MockHttpSession session =
                (MockHttpSession) loginResult
                        .getRequest()
                        .getSession(false);

        assertNotNull(session);

        assertEquals(
                userId,
                session.getAttribute(
                        AuthController.LOGIN_USER_ID
                )
        );

        assertEquals(
                "USER",
                session.getAttribute(
                        AuthController.LOGIN_USER_ROLE
                )
        );

        // 登录后查询当前用户
        mockMvc.perform(
                        get("/api/auth/me")
                                .session(session))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(userId)
                )
                .andExpect(
                        jsonPath("$.username")
                                .value(username)
                )
                .andExpect(
                        jsonPath("$.nickname")
                                .value("接口测试用户")
                )
                .andExpect(
                        jsonPath("$.passwordHash")
                                .doesNotExist()
                );

        // 错误密码登录
        String wrongPasswordBody = """
                {
                  "username": "%s",
                  "password": "wrong123"
                }
                """.formatted(username);

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(wrongPasswordBody))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value("用户名或密码错误")
                );

        // 重复注册
        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(registerBody))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value("用户名已经存在")
                );

        // 退出登录
        mockMvc.perform(
                        post("/api/auth/logout")
                                .session(session))
                .andExpect(status().isOk());

        // 退出后不携带Session查询，应返回401
        mockMvc.perform(
                        get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.message")
                                .value("请先登录")
                );
    }
}