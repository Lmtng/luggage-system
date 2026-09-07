package com.luggage.luggagesystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 取件验证响应
 *
 * 验证取件码通过后，返回费用信息供用户确认
 *
 * @author 成员B
 * @date 2026-09-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickupVerifyResponse {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 柜格编号
     */
    private String cellNo;

    /**
     * 寄存开始时间
     */
    private LocalDateTime startTime;

    /**
     * 当前时间（作为结束时间计算费用）
     */
    private LocalDateTime currentTime;

    /**
     * 实际寄存时长（分钟）
     */
    private Long actualMinutes;

    /**
     * 免费时长（分钟）
     */
    private Integer freeMinutes;

    /**
     * 收费时长（分钟）
     */
    private Long chargeableMinutes;

    /**
     * 计费单位（分钟）
     */
    private Integer unitMinutes;

    /**
     * 单价（元/单位）
     */
    private BigDecimal unitPrice;

    /**
     * 封顶金额（元）
     */
    private BigDecimal capAmount;

    /**
     * 计算出的费用（元）
     */
    private BigDecimal amount;

    /**
     * 提示信息
     */
    private String message;
}