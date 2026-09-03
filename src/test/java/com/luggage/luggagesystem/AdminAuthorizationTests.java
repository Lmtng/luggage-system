package com.luggage.luggagesystem;

import com.luggage.luggagesystem.controller.AuthController;
import com.luggage.luggagesystem.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminAuthorizationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unauthenticatedUserCannotAccessAdminApi()
            throws Exception {

        mockMvc.perform(
                        get("/api/admin/lockers"))
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.status").value(401)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("请先登录")
                );
    }

    @Test
    void normalUserCannotAccessAdminApi()
            throws Exception {

        MockHttpSession userSession =
                createSession(UserRole.USER);

        mockMvc.perform(
                        get("/api/admin/lockers")
                                .session(userSession))
                .andExpect(status().isForbidden())
                .andExpect(
                        jsonPath("$.status").value(403)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("需要管理员权限")
                );
    }

    @Test
    void administratorCanAccessAdminApi()
            throws Exception {

        MockHttpSession adminSession =
                createSession(UserRole.ADMIN);

        mockMvc.perform(
                        get("/api/admin/lockers")
                                .session(adminSession))
                .andExpect(status().isOk());
    }

    private MockHttpSession createSession(
            UserRole role) {

        MockHttpSession session =
                new MockHttpSession();

        session.setAttribute(
                AuthController.LOGIN_USER_ID,
                1L
        );

        session.setAttribute(
                AuthController.LOGIN_USER_ROLE,
                role.name()
        );

        return session;
    }
}