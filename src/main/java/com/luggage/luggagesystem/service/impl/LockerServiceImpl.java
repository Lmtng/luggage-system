package com.luggage.luggagesystem.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.luggage.luggagesystem.entity.Locker;
import com.luggage.luggagesystem.enums.LockerStatus;
import com.luggage.luggagesystem.mapper.LockerMapper;
import com.luggage.luggagesystem.service.LockerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class LockerServiceImpl implements LockerService {

    private final LockerMapper lockerMapper;

    public LockerServiceImpl(LockerMapper lockerMapper) {
        this.lockerMapper = lockerMapper;
    }

    @Override
    @Transactional
    public Locker createLocker(Locker locker) {
        if (locker == null) {
            throw new IllegalArgumentException("寄存柜信息不能为空");
        }

        if (!StringUtils.hasText(locker.getLockerCode())) {
            throw new IllegalArgumentException("寄存柜编号不能为空");
        }

        if (!StringUtils.hasText(locker.getName())) {
            throw new IllegalArgumentException("寄存柜名称不能为空");
        }

        if (!StringUtils.hasText(locker.getLocation())) {
            throw new IllegalArgumentException("寄存柜位置不能为空");
        }

        // 防止调用者自行指定数据库主键
        locker.setId(null);

        if (locker.getStatus() == null) {
            locker.setStatus(LockerStatus.ENABLED);
        }

        lockerMapper.insert(locker);
        return locker;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Locker> listLockers() {
        return lockerMapper.selectList(
                Wrappers.<Locker>lambdaQuery()
                        .orderByAsc(Locker::getId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Locker getLockerById(Long lockerId) {
        if (lockerId == null) {
            return null;
        }

        return lockerMapper.selectById(lockerId);
    }

    @Override
    @Transactional
    public boolean changeLockerStatus(
            Long lockerId,
            LockerStatus status) {

        if (lockerId == null || status == null) {
            throw new IllegalArgumentException("寄存柜编号和状态不能为空");
        }

        int affectedRows = lockerMapper.update(
                null,
                Wrappers.<Locker>lambdaUpdate()
                        .eq(Locker::getId, lockerId)
                        .set(Locker::getStatus, status)
        );

        return affectedRows == 1;
    }
}