package com.luggage.luggagesystem.service.impl;

import com.luggage.luggagesystem.mapper.LockerCellMapper;
import com.luggage.luggagesystem.service.LockerCellService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 柜格业务服务实现。
 */
@Service
public class LockerCellServiceImpl implements LockerCellService {

    private final LockerCellMapper lockerCellMapper;

    public LockerCellServiceImpl(LockerCellMapper lockerCellMapper) {
        this.lockerCellMapper = lockerCellMapper;
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
}