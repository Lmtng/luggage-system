package com.luggage.luggagesystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 创建订单响应
 *
 * 创建成功后返回给前端的信息
 *
 * @author 成员B
 * @date 2026-09-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderResponse {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 取件码（6位数字，只在此处返回给用户，后续不再展示）
     */
    private String pickupCode;

    /**
     * 柜格编号
     */
    private String cellNo;

    /**
     * 寄存开始时间
     */
    private LocalDateTime startTime;

    /**
     * 订单状态
     */
    private String status;

    /**
     * 提示信息
     */
    private String message;
}