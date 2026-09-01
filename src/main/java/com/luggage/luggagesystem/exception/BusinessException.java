package com.luggage.luggagesystem.exception;

import lombok.Getter;

/**
 * 业务异常类
 *
 * 用于表示业务逻辑校验失败的情况，如：
 * - 订单不存在
 * - 取件码错误
 * - 柜格已被占用
 * - 订单状态不正确
 *
 * @author 成员B
 * @date 2026-09-01
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final String code;

    public BusinessException(String message) {
        super(message);
        this.code = "BUSINESS_ERROR";
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = "BUSINESS_ERROR";
    }

    // ============ 预定义错误码 ============

    /**
     * 订单不存在
     */
    public static final String ORDER_NOT_FOUND = "ORDER_NOT_FOUND";

    /**
     * 订单不属于当前用户
     */
    public static final String ORDER_NOT_OWNER = "ORDER_NOT_OWNER";

    /**
     * 订单状态错误
     */
    public static final String ORDER_STATUS_ERROR = "ORDER_STATUS_ERROR";

    /**
     * 取件码错误
     */
    public static final String PICKUP_CODE_ERROR = "PICKUP_CODE_ERROR";

    /**
     * 取件码已过期
     */
    public static final String PICKUP_CODE_EXPIRED = "PICKUP_CODE_EXPIRED";

    /**
     * 柜格已被占用
     */
    public static final String CELL_OCCUPIED = "CELL_OCCUPIED";

    /**
     * 无空闲柜格
     */
    public static final String NO_AVAILABLE_CELL = "NO_AVAILABLE_CELL";

    /**
     * 订单已完成，不能重复操作
     */
    public static final String ORDER_ALREADY_COMPLETED = "ORDER_ALREADY_COMPLETED";
}