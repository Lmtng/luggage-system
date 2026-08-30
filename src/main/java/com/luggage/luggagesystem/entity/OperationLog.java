package com.luggage.luggagesystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long operatorId;          // 操作人ID

    private String operationType;     // 操作类型

    private String targetType;        // 目标类型

    private Long targetId;            // 目标记录ID

    private String detail;            // 操作说明

    private LocalDateTime createdAt;
}