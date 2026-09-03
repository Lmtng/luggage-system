package com.luggage.luggagesystem;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.luggage.luggagesystem.entity.Locker;
import com.luggage.luggagesystem.entity.LockerCell;
import com.luggage.luggagesystem.enums.CellSizeType;
import com.luggage.luggagesystem.enums.CellStatus;
import com.luggage.luggagesystem.enums.LockerStatus;
import com.luggage.luggagesystem.exception.NoAvailableCellException;
import com.luggage.luggagesystem.mapper.LockerCellMapper;
import com.luggage.luggagesystem.mapper.LockerMapper;
import com.luggage.luggagesystem.service.LockerCellService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class LockerCellAllocationTests {

    @Autowired
    private LockerCellService lockerCellService;

    @Autowired
    private LockerMapper lockerMapper;

    @Autowired
    private LockerCellMapper lockerCellMapper;

    @Test
    void automaticCellAllocationWorks() {
        /*
         * 暂时停用数据库中原有的可用MEDIUM柜格，
         * 避免本地演示数据影响测试。
         * 因为测试有@Transactional，结束后会自动回滚。
         */
        lockerCellMapper.update(
                null,
                Wrappers.<LockerCell>lambdaUpdate()
                        .eq(
                                LockerCell::getSizeType,
                                CellSizeType.MEDIUM
                        )
                        .eq(
                                LockerCell::getStatus,
                                CellStatus.AVAILABLE
                        )
                        .set(
                                LockerCell::getStatus,
                                CellStatus.DISABLED
                        )
        );

        Locker enabledLocker =
                createLocker(LockerStatus.ENABLED);

        Locker disabledLocker =
                createLocker(LockerStatus.DISABLED);

        LockerCell firstCell = createCell(
                enabledLocker.getId(),
                "D01"
        );

        LockerCell secondCell = createCell(
                enabledLocker.getId(),
                "D02"
        );

        LockerCell disabledLockerCell = createCell(
                disabledLocker.getId(),
                "X01"
        );

        // 第一次自动分配D01
        LockerCell firstAllocated =
                lockerCellService.allocateAvailableCell(
                        CellSizeType.MEDIUM
                );

        assertEquals(
                firstCell.getId(),
                firstAllocated.getId()
        );

        assertEquals(
                CellStatus.OCCUPIED,
                firstAllocated.getStatus()
        );

        // 第二次自动分配D02
        LockerCell secondAllocated =
                lockerCellService.allocateAvailableCell(
                        CellSizeType.MEDIUM
                );

        assertEquals(
                secondCell.getId(),
                secondAllocated.getId()
        );

        assertEquals(
                CellStatus.OCCUPIED,
                secondAllocated.getStatus()
        );

        // 停用寄存柜中的X01不能被分配
        LockerCell unchangedCell =
                lockerCellMapper.selectById(
                        disabledLockerCell.getId()
                );

        assertEquals(
                CellStatus.AVAILABLE,
                unchangedCell.getStatus()
        );

        // 启用寄存柜已经没有MEDIUM可用柜格
        assertThrows(
                NoAvailableCellException.class,
                () -> lockerCellService
                        .allocateAvailableCell(
                                CellSizeType.MEDIUM
                        )
        );
    }

    private Locker createLocker(
            LockerStatus status) {

        Locker locker = new Locker();

        locker.setLockerCode(
                "ALLOC-" + status + "-"
                        + System.nanoTime()
        );
        locker.setName("自动分配测试柜");
        locker.setLocation("测试位置");
        locker.setStatus(status);

        lockerMapper.insert(locker);

        return locker;
    }

    private LockerCell createCell(
            Long lockerId,
            String cellNo) {

        LockerCell cell = new LockerCell();

        cell.setLockerId(lockerId);
        cell.setCellNo(cellNo);
        cell.setSizeType(CellSizeType.MEDIUM);
        cell.setStatus(CellStatus.AVAILABLE);
        cell.setVersion(0);

        lockerCellMapper.insert(cell);

        return cell;
    }
}