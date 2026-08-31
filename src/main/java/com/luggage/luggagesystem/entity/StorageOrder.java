package com.luggage.luggagesystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 寄存订单实体类
 * 对应表：storage_order
 */
@Data
@TableName("storage_order")
public class StorageOrder {

    /**
     * 订单ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号，唯一索引
     * 格式：ST + 时间戳 + 随机序列，如 ST202608290001
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 用户ID，外键关联 sys_user.id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 柜格ID，外键关联 locker_cell.id
     */
    @TableField("cell_id")
    private Long cellId;

    /**
     * 取件码摘要（BCrypt加密）
     * 取件码为6位随机数字，存储其加密摘要
     */
    @TableField("pickup_code_hash")
    private String pickupCodeHash;

    /**
     * 寄存开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 实际结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 最终费用
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 模拟支付状态：UNPAID（未支付）、PAID（已支付）
     */
    @TableField("payment_status")
    private String paymentStatus;

    /**
     * 订单状态：
     * STORED（寄存中）、PENDING_PAYMENT（待支付）
     * COMPLETED（已完成）、CANCELLED（已取消）
     * EXCEPTION（异常）
     */
    @TableField("status")
    private String status;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // ============ 状态常量 ============

    /**
     * 支付状态常量
     */
    public static final class PaymentStatus {
        public static final String UNPAID = "UNPAID";
        public static final String PAID = "PAID";
    }

    /**
     * 订单状态常量
     */
    public static final class OrderStatus {
        public static final String STORED = "STORED";                  // 寄存中
        public static final String PENDING_PAYMENT = "PENDING_PAYMENT"; // 待支付
        public static final String COMPLETED = "COMPLETED";            // 已完成
        public static final String CANCELLED = "CANCELLED";            // 已取消
        public static final String EXCEPTION = "EXCEPTION";            // 异常
    }
}