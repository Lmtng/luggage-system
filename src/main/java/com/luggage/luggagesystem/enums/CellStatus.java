package com.luggage.luggagesystem.enums;

/**
 * 柜格状态。
 */
public enum CellStatus {

    /**
     * 空闲，可以分配。
     */
    AVAILABLE,

    /**
     * 已被订单占用。
     */
    OCCUPIED,

    /**
     * 已被管理员停用。
     */
    DISABLED
}