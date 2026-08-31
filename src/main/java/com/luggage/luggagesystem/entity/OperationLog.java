package com.luggage.luggagesystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 操作日志实体类
 * 对应表：operation_log
 */
@Data
@TableName("operation_log")
public class OperationLog {

    /**
     * 日志ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 操作人ID，关联 sys_user.id
     */
    @TableField("operator_id")
    private Long operatorId;

    /**
     * 操作类型：如 UPDATE_CELL、UPDATE_PRICE_RULE、FIX_EXCEPTION_ORDER
     */
    @TableField("operation_type")
    private String operationType;

    /**
     * 目标类型：如 CELL、PRICE_RULE、ORDER
     */
    @TableField("target_type")
    private String targetType;

    /**
     * 目标记录ID
     */
    @TableField("target_id")
    private Long targetId;

    /**
     * 操作详细说明
     */
    @TableField("detail")
    private String detail;

    /**
     * 操作时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // ============ 常量 ============

    public static final class OperationType {
        public static final String UPDATE_CELL = "UPDATE_CELL";
        public static final String UPDATE_PRICE_RULE = "UPDATE_PRICE_RULE";
        public static final String FIX_EXCEPTION_ORDER = "FIX_EXCEPTION_ORDER";
        public static final String CREATE_LOCKER = "CREATE_LOCKER";
        public static final String DISABLE_CELL = "DISABLE_CELL";
        public static final String ENABLE_CELL = "ENABLE_CELL";
    }

    public static final class TargetType {
        public static final String CELL = "CELL";
        public static final String PRICE_RULE = "PRICE_RULE";
        public static final String ORDER = "ORDER";
        public static final String LOCKER = "LOCKER";
    }
}