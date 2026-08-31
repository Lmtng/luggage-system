package com.luggage.luggagesystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.luggage.luggagesystem.entity.StorageOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 寄存订单Mapper
 */
@Mapper
public interface StorageOrderMapper extends BaseMapper<StorageOrder> {

    /**
     * 根据订单号查询订单（用于唯一索引校验）
     */
    @Select("SELECT * FROM storage_order WHERE order_no = #{orderNo}")
    StorageOrder selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 查询用户在某个柜格的寄存中订单（防止重复寄存）
     */
    @Select("SELECT * FROM storage_order WHERE user_id = #{userId} AND cell_id = #{cellId} AND status IN ('STORED', 'PENDING_PAYMENT')")
    List<StorageOrder> selectActiveOrdersByUserAndCell(@Param("userId") Long userId, @Param("cellId") Long cellId);

    /**
     * 根据柜格ID查询未完成的订单（用于释放柜格前检查）
     */
    @Select("SELECT * FROM storage_order WHERE cell_id = #{cellId} AND status IN ('STORED', 'PENDING_PAYMENT')")
    List<StorageOrder> selectUnfinishedOrdersByCellId(@Param("cellId") Long cellId);

    /**
     * 更新订单状态（带乐观锁条件）
     */
    @Update("UPDATE storage_order SET status = #{newStatus}, updated_at = NOW() " +
            "WHERE id = #{orderId} AND status = #{oldStatus}")
    int updateStatus(@Param("orderId") Long orderId,
                     @Param("oldStatus") String oldStatus,
                     @Param("newStatus") String newStatus);

    /**
     * 完成订单（支付并释放）
     */
    @Update("UPDATE storage_order SET status = 'COMPLETED', payment_status = 'PAID', " +
            "end_time = #{endTime}, amount = #{amount}, updated_at = NOW() " +
            "WHERE id = #{orderId} AND status = 'PENDING_PAYMENT'")
    int completeOrder(@Param("orderId") Long orderId,
                      @Param("endTime") LocalDateTime endTime,
                      @Param("amount") java.math.BigDecimal amount);
}