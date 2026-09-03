package com.luggage.luggagesystem;

import com.luggage.luggagesystem.controller.AuthController;
import com.luggage.luggagesystem.entity.Locker;
import com.luggage.luggagesystem.entity.LockerCell;
import com.luggage.luggagesystem.enums.CellSizeType;
import com.luggage.luggagesystem.enums.CellStatus;
import com.luggage.luggagesystem.enums.LockerStatus;
import com.luggage.luggagesystem.mapper.LockerCellMapper;
import com.luggage.luggagesystem.mapper.LockerMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserOperationAuthorizationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LockerMapper lockerMapper;

    @Autowired
    private LockerCellMapper lockerCellMapper;

    @Test
    void loginIsRequiredToOccupyAndReleaseCell()
            throws Exception {

        LockerCell cell = createAvailableCell();

        // 查询可用柜格不要求登录
        mockMvc.perform(
                        get("/api/locker-cells/available"))
                .andExpect(status().isOk());

        // 未登录不能占用柜格
        mockMvc.perform(
                        post(
                                "/api/locker-cells/{cellId}/occupy",
                                cell.getId()
                        ))
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.message")
                                .value("请先登录")
                );

        MockHttpSession userSession =
                createUserSession();

        // 登录后可以占用柜格
        mockMvc.perform(
                        post(
                                "/api/locker-cells/{cellId}/occupy",
                                cell.getId()
                        ).session(userSession))
                .andExpect(status().isOk());

        // 未登录不能释放柜格
        mockMvc.perform(
                        post(
                                "/api/locker-cells/{cellId}/release",
                                cell.getId()
                        ))
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.message")
                                .value("请先登录")
                );

        // 登录后可以释放柜格
        mockMvc.perform(
                        post(
                                "/api/locker-cells/{cellId}/release",
                                cell.getId()
                        ).session(userSession))
                .andExpect(status().isOk());
    }

    private LockerCell createAvailableCell() {
        Locker locker = new Locker();
        locker.setLockerCode(
                "AUTH-" + System.nanoTime()
        );
        locker.setName("权限测试寄存柜");
        locker.setLocation("测试位置");
        locker.setStatus(LockerStatus.ENABLED);

        lockerMapper.insert(locker);

        LockerCell cell = new LockerCell();
        cell.setLockerId(locker.getId());
        cell.setCellNo("C01");
        cell.setSizeType(CellSizeType.SMALL);
        cell.setStatus(CellStatus.AVAILABLE);
        cell.setVersion(0);

        lockerCellMapper.insert(cell);

        return cell;
    }

    private MockHttpSession createUserSession() {
        MockHttpSession session =
                new MockHttpSession();

        session.setAttribute(
                AuthController.LOGIN_USER_ID,
                1L
        );

        session.setAttribute(
                AuthController.LOGIN_USER_ROLE,
                "USER"
        );

        return session;
    }
}