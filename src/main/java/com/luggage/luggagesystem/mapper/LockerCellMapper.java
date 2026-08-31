package com.luggage.luggagesystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.luggage.luggagesystem.entity.LockerCell;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 柜格数据访问接口。
 */
@Mapper
public interface LockerCellMapper extends BaseMapper<LockerCell> {

    /**
     * 只在柜格空闲时占用柜格。
     *
     * @param cellId 柜格ID
     * @return 受影响行数，1表示成功，0表示失败
     */
    @Update("""
            UPDATE locker_cell
            SET status = 'OCCUPIED',
                version = version + 1
            WHERE id = #{cellId}
              AND status = 'AVAILABLE'
            """)
    int occupyIfAvailable(@Param("cellId") Long cellId);

    /**
     * 只在柜格被占用时释放柜格。
     *
     * @param cellId 柜格ID
     * @return 受影响行数，1表示成功，0表示失败
     */
    @Update("""
            UPDATE locker_cell
            SET status = 'AVAILABLE',
                version = version + 1
            WHERE id = #{cellId}
              AND status = 'OCCUPIED'
            """)
    int releaseIfOccupied(@Param("cellId") Long cellId);
}