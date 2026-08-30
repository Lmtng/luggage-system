package com.luggage.luggagesystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("price_rule")
public class PriceRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sizeType;          // SMALL / MEDIUM / LARGE

    private Integer unitMinutes;      // 一个计费单位的分钟数

    private BigDecimal unitPrice;     // 每单位价格

    private Integer freeMinutes;      // 免费分钟数

    private BigDecimal capAmount;     // 单次封顶金额

    private Integer enabled;          // 是否启用 1=启用 0=停用

    private LocalDateTime updatedAt;

    // 业务方法（后面实现）
    // public BigDecimal calculateFee(LocalDateTime start, LocalDateTime end) { ... }
}