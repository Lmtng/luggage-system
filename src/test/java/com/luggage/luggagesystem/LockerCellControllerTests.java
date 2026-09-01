package com.luggage.luggagesystem;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LockerCellControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LockerMapper lockerMapper;

    @Autowired
    private LockerCellMapper lockerCellMapper;

    @Test
    void occupyAndReleaseApiWorks() throws Exception {
        // 准备一个启用的寄存柜
        Locker locker = new Locker();
        locker.setLockerCode("WEB-" + System.nanoTime());
        locker.setName("接口测试柜");
        locker.setLocation("测试地点");
        locker.setStatus(LockerStatus.ENABLED);
        lockerMapper.insert(locker);

        // 准备一个可用柜格
        LockerCell cell = new LockerCell();
        cell.setLockerId(locker.getId());
        cell.setCellNo("A01");
        cell.setSizeType(CellSizeType.SMALL);
        cell.setStatus(CellStatus.AVAILABLE);
        cell.setVersion(0);
        lockerCellMapper.insert(cell);

        // 第一次占用应当成功
        mockMvc.perform(post(
                        "/api/locker-cells/{cellId}/occupy",
                        cell.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("柜格占用成功"));

        // 重复占用应当返回 409
        mockMvc.perform(post(
                        "/api/locker-cells/{cellId}/occupy",
                        cell.getId()))
                .andExpect(status().isConflict());

        // 释放已占用的柜格应当成功
        mockMvc.perform(post(
                        "/api/locker-cells/{cellId}/release",
                        cell.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("柜格释放成功"));

        // 重复释放应当返回 409
        mockMvc.perform(post(
                        "/api/locker-cells/{cellId}/release",
                        cell.getId()))
                .andExpect(status().isConflict());
    }
}