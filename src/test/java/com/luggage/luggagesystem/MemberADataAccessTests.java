package com.luggage.luggagesystem;
import java.util.List;

import com.luggage.luggagesystem.entity.Locker;
import com.luggage.luggagesystem.entity.LockerCell;
import com.luggage.luggagesystem.entity.SysUser;
import com.luggage.luggagesystem.enums.CellSizeType;
import com.luggage.luggagesystem.enums.CellStatus;
import com.luggage.luggagesystem.enums.LockerStatus;
import com.luggage.luggagesystem.enums.UserRole;
import com.luggage.luggagesystem.enums.UserStatus;
import com.luggage.luggagesystem.mapper.LockerCellMapper;
import com.luggage.luggagesystem.mapper.LockerMapper;
import com.luggage.luggagesystem.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import com.luggage.luggagesystem.service.LockerCellService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
class MemberADataAccessTests {
    @Autowired
    private LockerCellService lockerCellService;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private LockerMapper lockerMapper;

    @Autowired
    private LockerCellMapper lockerCellMapper;

    @Test
    void userLockerAndCellCrudWorks() {
        SysUser user = new SysUser();
        user.setUsername("mapper_test_" + System.nanoTime());
        user.setPasswordHash("test-password-hash");
        user.setNickname("测试用户");
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.NORMAL);

        int userRows = sysUserMapper.insert(user);

        assertEquals(1, userRows);
        assertNotNull(user.getId());

        SysUser savedUser = sysUserMapper.selectById(user.getId());

        assertNotNull(savedUser);
        assertEquals(user.getUsername(), savedUser.getUsername());
        assertEquals(UserRole.USER, savedUser.getRole());
        assertEquals(UserStatus.NORMAL, savedUser.getStatus());


        Locker locker = new Locker();
        locker.setLockerCode("TEST_" + System.nanoTime());
        locker.setName("测试寄存柜");
        locker.setLocation("测试位置");
        locker.setStatus(LockerStatus.ENABLED);

        int lockerRows = lockerMapper.insert(locker);

        assertEquals(1, lockerRows);
        assertNotNull(locker.getId());

        Locker savedLocker = lockerMapper.selectById(locker.getId());

        assertNotNull(savedLocker);
        assertEquals(locker.getLockerCode(), savedLocker.getLockerCode());
        assertEquals(LockerStatus.ENABLED, savedLocker.getStatus());


        LockerCell cell = new LockerCell();
        cell.setLockerId(locker.getId());
        cell.setCellNo("A01");
        cell.setSizeType(CellSizeType.MEDIUM);
        cell.setStatus(CellStatus.AVAILABLE);
        cell.setVersion(0);

        int cellRows = lockerCellMapper.insert(cell);

        assertEquals(1, cellRows);
        assertNotNull(cell.getId());

        LockerCell savedCell = lockerCellMapper.selectById(cell.getId());

        assertNotNull(savedCell);
        assertEquals(locker.getId(), savedCell.getLockerId());
        assertEquals("A01", savedCell.getCellNo());
        assertEquals(CellSizeType.MEDIUM, savedCell.getSizeType());
        assertEquals(CellStatus.AVAILABLE, savedCell.getStatus());
        assertEquals(0, savedCell.getVersion());
    }
    @Test
    void occupyAndReleaseCellWorks() {
        Locker locker = new Locker();
        locker.setLockerCode("STATUS_TEST_" + System.nanoTime());
        locker.setName("状态测试寄存柜");
        locker.setLocation("测试位置");
        locker.setStatus(LockerStatus.ENABLED);

        assertEquals(1, lockerMapper.insert(locker));
        assertNotNull(locker.getId());

        LockerCell cell = new LockerCell();
        cell.setLockerId(locker.getId());
        cell.setCellNo("B01");
        cell.setSizeType(CellSizeType.SMALL);
        cell.setStatus(CellStatus.AVAILABLE);
        cell.setVersion(0);

        assertEquals(1, lockerCellMapper.insert(cell));
        assertNotNull(cell.getId());

        boolean firstOccupy = lockerCellService.occupyCell(cell.getId());
        boolean secondOccupy = lockerCellService.occupyCell(cell.getId());

        assertTrue(firstOccupy);
        assertFalse(secondOccupy);

        LockerCell occupiedCell = lockerCellMapper.selectById(cell.getId());

        assertEquals(CellStatus.OCCUPIED, occupiedCell.getStatus());
        assertEquals(1, occupiedCell.getVersion());

        boolean firstRelease = lockerCellService.releaseCell(cell.getId());
        boolean secondRelease = lockerCellService.releaseCell(cell.getId());

        assertTrue(firstRelease);
        assertFalse(secondRelease);

        LockerCell releasedCell = lockerCellMapper.selectById(cell.getId());

        assertEquals(CellStatus.AVAILABLE, releasedCell.getStatus());
        assertEquals(2, releasedCell.getVersion());
    }
    @Test
    void availableCellQueryOnlyReturnsUsableCells() {
        Locker enabledLocker = new Locker();
        enabledLocker.setLockerCode("QUERY_ENABLED_" + System.nanoTime());
        enabledLocker.setName("启用寄存柜");
        enabledLocker.setLocation("测试位置");
        enabledLocker.setStatus(LockerStatus.ENABLED);
        lockerMapper.insert(enabledLocker);

        Locker disabledLocker = new Locker();
        disabledLocker.setLockerCode("QUERY_DISABLED_" + System.nanoTime());
        disabledLocker.setName("停用寄存柜");
        disabledLocker.setLocation("测试位置");
        disabledLocker.setStatus(LockerStatus.DISABLED);
        lockerMapper.insert(disabledLocker);

        LockerCell availableSmallCell = new LockerCell();
        availableSmallCell.setLockerId(enabledLocker.getId());
        availableSmallCell.setCellNo("S01");
        availableSmallCell.setSizeType(CellSizeType.SMALL);
        availableSmallCell.setStatus(CellStatus.AVAILABLE);
        availableSmallCell.setVersion(0);
        lockerCellMapper.insert(availableSmallCell);

        LockerCell occupiedSmallCell = new LockerCell();
        occupiedSmallCell.setLockerId(enabledLocker.getId());
        occupiedSmallCell.setCellNo("S02");
        occupiedSmallCell.setSizeType(CellSizeType.SMALL);
        occupiedSmallCell.setStatus(CellStatus.OCCUPIED);
        occupiedSmallCell.setVersion(0);
        lockerCellMapper.insert(occupiedSmallCell);

        LockerCell availableLargeCell = new LockerCell();
        availableLargeCell.setLockerId(enabledLocker.getId());
        availableLargeCell.setCellNo("L01");
        availableLargeCell.setSizeType(CellSizeType.LARGE);
        availableLargeCell.setStatus(CellStatus.AVAILABLE);
        availableLargeCell.setVersion(0);
        lockerCellMapper.insert(availableLargeCell);

        LockerCell disabledLockerCell = new LockerCell();
        disabledLockerCell.setLockerId(disabledLocker.getId());
        disabledLockerCell.setCellNo("S01");
        disabledLockerCell.setSizeType(CellSizeType.SMALL);
        disabledLockerCell.setStatus(CellStatus.AVAILABLE);
        disabledLockerCell.setVersion(0);
        lockerCellMapper.insert(disabledLockerCell);

        List<LockerCell> smallCells =
                lockerCellService.listAvailableCells(CellSizeType.SMALL);

        assertTrue(containsCell(smallCells, availableSmallCell.getId()));
        assertFalse(containsCell(smallCells, occupiedSmallCell.getId()));
        assertFalse(containsCell(smallCells, availableLargeCell.getId()));
        assertFalse(containsCell(smallCells, disabledLockerCell.getId()));

        List<LockerCell> allCells =
                lockerCellService.listAvailableCells(null);

        assertTrue(containsCell(allCells, availableSmallCell.getId()));
        assertTrue(containsCell(allCells, availableLargeCell.getId()));
        assertFalse(containsCell(allCells, occupiedSmallCell.getId()));
        assertFalse(containsCell(allCells, disabledLockerCell.getId()));
    }
    private boolean containsCell(List<LockerCell> cells, Long cellId) {
        return cells.stream()
                .anyMatch(cell -> cellId.equals(cell.getId()));
    }}