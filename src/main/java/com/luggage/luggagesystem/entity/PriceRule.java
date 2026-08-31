package com.luggage.luggagesystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 计费规则实体类
 * 对应表：price_rule
 */
@Data
@TableName("price_rule")
public class PriceRule {

    /**
     * 规则ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 柜格规格：SMALL（小）、MEDIUM（中）、LARGE（大）
     */
    @TableField("size_type")
    private String sizeType;

    /**
     * 一个计费单位的分钟数，默认60分钟
     */
    @TableField("unit_minutes")
    private Integer unitMinutes;

    /**
     * 每单位价格
     */
    @TableField("unit_price")
    private BigDecimal unitPrice;

    /**
     * 免费分钟数
     */
    @TableField("free_minutes")
    private Integer freeMinutes;

    /**
     * 单次封顶金额，可为空（表示不封顶）
     */
    @TableField("cap_amount")
    private BigDecimal capAmount;

    /**
     * 是否启用：1启用，0停用
     */
    @TableField("enabled")
    private Integer enabled;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // ============ 规格常量 ============

    public static final class SizeType {
        public static final String SMALL = "SMALL";
        public static final String MEDIUM = "MEDIUM";
        public static final String LARGE = "LARGE";
    }

    public static final class EnabledStatus {
        public static final Integer ENABLED = 1;
        public static final Integer DISABLED = 0;
    }
}