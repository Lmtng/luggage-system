package com.luggage.luggagesystem;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luggage.luggagesystem.controller.AuthController;
import com.luggage.luggagesystem.entity.Locker;
import com.luggage.luggagesystem.enums.LockerStatus;
import com.luggage.luggagesystem.mapper.LockerMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminLockerCellControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LockerMapper lockerMapper;

    @Test
    void lockerCellManagementApiWorks() throws Exception {
        MockHttpSession adminSession =
                createAdminSession();

        // 创建一个测试寄存柜
        Locker locker = new Locker();
        locker.setLockerCode(
                "CELL-" + System.nanoTime()
        );
        locker.setName("柜格接口测试柜");
        locker.setLocation("教学楼三层");
        locker.setStatus(LockerStatus.ENABLED);

        lockerMapper.insert(locker);

        String requestBody = """
                {
                  "lockerId": %d,
                  "cellNo": "B01",
                  "sizeType": "MEDIUM"
                }
                """.formatted(locker.getId());

        // 新增柜格
        String responseBody = mockMvc.perform(
                        post("/api/admin/locker-cells")
                                .session(adminSession)
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.cellNo")
                                .value("B01")
                )
                .andExpect(
                        jsonPath("$.sizeType")
                                .value("MEDIUM")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("AVAILABLE")
                )
                .andReturn()
                .getResponse()
                .getContentAsString(
                        StandardCharsets.UTF_8
                );

        JsonNode createdCell =
                objectMapper.readTree(responseBody);

        long cellId =
                createdCell.get("id").asLong();

        // 根据柜格ID查询
        mockMvc.perform(
                        get(
                                "/api/admin/locker-cells/{cellId}",
                                cellId
                        ).session(adminSession))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(cellId)
                )
                .andExpect(
                        jsonPath("$.cellNo")
                                .value("B01")
                );

        // 查询指定寄存柜下的柜格
        mockMvc.perform(
                        get("/api/admin/locker-cells")
                                .session(adminSession)
                                .param(
                                        "lockerId",
                                        locker.getId().toString()
                                ))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[0].id")
                                .value(cellId)
                );

        // 管理员停用柜格
        mockMvc.perform(
                        put(
                                "/api/admin/locker-cells/{cellId}/status",
                                cellId
                        )
                                .session(adminSession)
                                .param(
                                        "status",
                                        "DISABLED"
                                ))
                .andExpect(status().isOk());

        // 确认状态已经变成DISABLED
        mockMvc.perform(
                        get(
                                "/api/admin/locker-cells/{cellId}",
                                cellId
                        ).session(adminSession))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("DISABLED")
                );

        // 查询不存在的柜格应返回404
        mockMvc.perform(
                        get(
                                "/api/admin/locker-cells/{cellId}",
                                999999999L
                        ).session(adminSession))
                .andExpect(status().isNotFound());
    }

    private MockHttpSession createAdminSession() {
        MockHttpSession session =
                new MockHttpSession();

        session.setAttribute(
                AuthController.LOGIN_USER_ID,
                1L
        );

        session.setAttribute(
                AuthController.LOGIN_USER_ROLE,
                "ADMIN"
        );

        return session;
    }
}