package com.luggage.luggagesystem.dto;

import lombok.Data;

/**
 * 创建订单请求
 *
 * 用户选择柜格后，提交创建订单请求
 *
 * @author 成员B
 * @date 2026-09-01
 */
@Data
public class CreateOrderRequest {

    /**
     * 柜格ID（用户选择的柜格）
     */
    private Long cellId;

    /**
     * 用户ID（从登录凭证获取，不由前端传入）
     */
    private Long userId;
}