package com.luggage.luggagesystem.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderVO {
    private Long id;
    private String orderNo;
    private Long cellId;
    private String cellNo;
    private String sizeType;
    private String location;
    private String pickupCode;        // 取件码明文（只在创建时返回）
    private LocalDateTime startTime;
    private String status;
    private BigDecimal amount;
}