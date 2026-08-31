package com.luggage.luggagesystem.service;

/**
 * 柜格业务服务。
 */
public interface LockerCellService {

    /**
     * 尝试占用一个空闲柜格。
     *
     * @param cellId 柜格ID
     * @return true表示成功，false表示柜格不存在或当前不可用
     */
    boolean occupyCell(Long cellId);

    /**
     * 尝试释放一个被占用的柜格。
     *
     * @param cellId 柜格ID
     * @return true表示成功，false表示柜格不存在或当前未被占用
     */
    boolean releaseCell(Long cellId);
}