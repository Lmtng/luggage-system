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
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderResponse {

    private Long orderId;

    private String orderNo;

    private String pickupCode;

    private String cellNo;

    private LocalDateTime startTime;

    private String status;

    private String message;
}