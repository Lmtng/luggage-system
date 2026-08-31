package com.luggage.luggagesystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.luggage.luggagesystem.entity.Locker;
import org.apache.ibatis.annotations.Mapper;

/**
 * 寄存柜数据访问接口。
 */
@Mapper
public interface LockerMapper extends BaseMapper<Locker> {
}