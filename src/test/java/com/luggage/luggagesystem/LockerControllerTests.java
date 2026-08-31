package com.luggage.luggagesystem;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
class LockerControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void lockerManagementApiWorks() throws Exception {
        String lockerCode = "ADMIN-" + System.nanoTime();

        String requestBody = """
                {
                  "lockerCode": "%s",
                  "name": "管理员接口测试柜",
                  "location": "教学楼二层",
                  "status": "ENABLED"
                }
                """.formatted(lockerCode);

        // 新增寄存柜
        String responseBody = mockMvc.perform(
                        post("/api/admin/lockers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.lockerCode").value(lockerCode))
                .andExpect(jsonPath("$.status").value("ENABLED"))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JsonNode createdLocker =
                objectMapper.readTree(responseBody);

        long lockerId = createdLocker.get("id").asLong();

        // 根据 ID 查询
        mockMvc.perform(
                        get("/api/admin/lockers/{lockerId}", lockerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(lockerId))
                .andExpect(jsonPath("$.lockerCode").value(lockerCode));

        // 停用寄存柜
        mockMvc.perform(
                        put("/api/admin/lockers/{lockerId}/status", lockerId)
                                .param("status", "DISABLED"))
                .andExpect(status().isOk());

        // 确认状态已经改变
        mockMvc.perform(
                        get("/api/admin/lockers/{lockerId}", lockerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISABLED"));

        // 查询不存在的寄存柜，应返回 404
        mockMvc.perform(
                        get("/api/admin/lockers/{lockerId}", 999999999L))
                .andExpect(status().isNotFound());
    }
}