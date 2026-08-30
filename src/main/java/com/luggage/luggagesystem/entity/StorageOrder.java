package com.luggage.luggagesystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("storage_order")
public class StorageOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    private Long cellId;

    private String pickupCodeHash;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private BigDecimal amount;

    private String paymentStatus;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}