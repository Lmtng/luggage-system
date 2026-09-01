package com.luggage.luggagesystem.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一响应码
 *
 * @author 成员B
 * @date 2026-09-01
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 客户端错误 4xx
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),

    // 业务错误
    BUSINESS_ERROR(1001, "业务处理失败"),
    ORDER_NOT_FOUND(1002, "订单不存在"),
    ORDER_NOT_OWNER(1003, "无权操作该订单"),
    ORDER_STATUS_ERROR(1004, "订单状态错误"),
    PICKUP_CODE_ERROR(1005, "取件码错误"),
    CELL_OCCUPIED(1006, "柜格已被占用"),
    NO_AVAILABLE_CELL(1007, "无空闲柜格"),

    // 服务器错误 5xx
    INTERNAL_ERROR(500, "系统内部错误");

    private final Integer code;
    private final String message;
}