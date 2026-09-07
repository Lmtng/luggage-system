package com.luggage.luggagesystem.dto;

import lombok.Data;

/**
 * 取件验证请求
 *
 * 用户输入取件码，请求验证并计算费用
 *
 * @author 成员B
 * @date 2026-09-01
 */
@Data
public class PickupVerifyRequest {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 用户输入的取件码
     */
    private String pickupCode;

    /**
     * 用户ID（从登录凭证获取，不由前端传入）
     */
    private Long userId;
}