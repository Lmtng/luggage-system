package com.luggage.luggagesystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.luggage.luggagesystem.enums.CellSizeType;
import com.luggage.luggagesystem.enums.CellStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 柜格实体，对应locker_cell表。
 */
@Data
@TableName("locker_cell")
public class LockerCell {

    /**
     * 柜格主键。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属寄存柜ID。
     */
    private Long lockerId;

    /**
     * 柜格编号。
     */
    private String cellNo;

    /**
     * 柜格规格。
     */
    private CellSizeType sizeType;

    /**
     * 柜格状态。
     */
    private CellStatus status;

    /**
     * 乐观锁版本号。
     */
    @Version
    private Integer version;

    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    private LocalDateTime updatedAt;
}