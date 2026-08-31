package com.luggage.luggagesystem.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.luggage.luggagesystem.entity.LockerCell;
import com.luggage.luggagesystem.enums.CellSizeType;
import com.luggage.luggagesystem.enums.CellStatus;
import com.luggage.luggagesystem.mapper.LockerCellMapper;
import com.luggage.luggagesystem.mapper.LockerMapper;
import com.luggage.luggagesystem.service.LockerCellService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 柜格业务服务实现。
 */
@Service
public class LockerCellServiceImpl implements LockerCellService {

    private final LockerCellMapper lockerCellMapper;
    private final LockerMapper lockerMapper;

    public LockerCellServiceImpl(
            LockerCellMapper lockerCellMapper,
            LockerMapper lockerMapper) {

        this.lockerCellMapper = lockerCellMapper;
        this.lockerMapper = lockerMapper;
    }

    @Override
    @Transactional
    public boolean occupyCell(Long cellId) {
        if (cellId == null) {
            throw new IllegalArgumentException("柜格ID不能为空");
        }

        return lockerCellMapper.occupyIfAvailable(cellId) == 1;
    }

    @Override
    @Transactional
    public boolean releaseCell(Long cellId) {
        if (cellId == null) {
            throw new IllegalArgumentException("柜格ID不能为空");
        }

        return lockerCellMapper.releaseIfOccupied(cellId) == 1;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LockerCell> listAvailableCells(
            CellSizeType sizeType) {

        if (sizeType == null) {
            return lockerCellMapper.selectAvailableCells();
        }

        return lockerCellMapper
                .selectAvailableCellsBySize(sizeType);
    }

    @Override
    @Transactional
    public LockerCell createCell(LockerCell lockerCell) {
        if (lockerCell == null) {
            throw new IllegalArgumentException("柜格信息不能为空");
        }

        if (lockerCell.getLockerId() == null) {
            throw new IllegalArgumentException("所属寄存柜不能为空");
        }

        if (lockerMapper.selectById(
                lockerCell.getLockerId()) == null) {

            throw new IllegalArgumentException("所属寄存柜不存在");
        }

        if (!StringUtils.hasText(lockerCell.getCellNo())) {
            throw new IllegalArgumentException("柜格编号不能为空");
        }

        if (lockerCell.getSizeType() == null) {
            throw new IllegalArgumentException("柜格尺寸不能为空");
        }

        // 主键由数据库自动生成
        lockerCell.setId(null);

        if (lockerCell.getStatus() == null) {
            lockerCell.setStatus(CellStatus.AVAILABLE);
        }

        lockerCell.setVersion(0);

        lockerCellMapper.insert(lockerCell);

        return lockerCell;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LockerCell> listCellsByLockerId(
            Long lockerId) {

        if (lockerId == null) {
            throw new IllegalArgumentException("寄存柜编号不能为空");
        }

        return lockerCellMapper.selectList(
                Wrappers.<LockerCell>lambdaQuery()
                        .eq(LockerCell::getLockerId, lockerId)
                        .orderByAsc(LockerCell::getCellNo)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public LockerCell getCellById(Long cellId) {
        if (cellId == null) {
            return null;
        }

        return lockerCellMapper.selectById(cellId);
    }

    @Override
    @Transactional
    public boolean changeCellStatus(
            Long cellId,
            CellStatus status) {

        if (cellId == null || status == null) {
            throw new IllegalArgumentException(
                    "柜格编号和状态不能为空"
            );
        }

        // OCCUPIED只能由寄存业务产生
        if (status == CellStatus.OCCUPIED) {
            throw new IllegalArgumentException(
                    "占用状态只能由寄存业务修改"
            );
        }

        int affectedRows = lockerCellMapper.update(
                null,
                Wrappers.<LockerCell>lambdaUpdate()
                        .eq(LockerCell::getId, cellId)
                        // 已占用柜格不能被管理员直接停用
                        .ne(
                                LockerCell::getStatus,
                                CellStatus.OCCUPIED
                        )
                        .set(LockerCell::getStatus, status)
                        .setSql("version = version + 1")
        );

        return affectedRows == 1;
    }
}