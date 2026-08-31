package com.luggage.luggagesystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.luggage.luggagesystem.entity.LockerCell;
import org.apache.ibatis.annotations.Mapper;

/**
 * 柜格数据访问接口。
 */
@Mapper
public interface LockerCellMapper extends BaseMapper<LockerCell> {
}