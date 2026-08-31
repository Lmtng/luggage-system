package com.luggage.luggagesystem.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.luggage.luggagesystem.enums.LockerStatus;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@TableName("locker")
public class Locker {
    @TableId(type=IdType.AUTO)
    private Long id;
    /**
     * 寄存柜唯一编号。
     */
    private String lockerCode;
    /**
     * 寄存柜名称。
     */
    private String name;
    /**
     * 寄存柜位置。
     */
    private String location;
    /**
     * 寄存柜状态。
     */
    private LockerStatus status;
    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;
    /**
     * 更新时间。
     */
    private LocalDateTime updatedAt;
}
