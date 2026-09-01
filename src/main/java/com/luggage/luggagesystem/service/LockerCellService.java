package com.luggage.luggagesystem.service;

import com.luggage.luggagesystem.entity.LockerCell;
import com.luggage.luggagesystem.enums.CellSizeType;
import com.luggage.luggagesystem.enums.CellStatus;

import java.util.List;

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

    /**
     * 查询当前可以使用的柜格。
     *
     * @param sizeType 柜格规格；传入null表示查询全部规格
     * @return 可用柜格列表
     */
    List<LockerCell> listAvailableCells(CellSizeType sizeType);

    /**
     * 管理员新增柜格。
     *
     * @param lockerCell 柜格信息
     * @return 创建成功的柜格
     */
    LockerCell createCell(LockerCell lockerCell);

    /**
     * 查询指定寄存柜下的全部柜格。
     *
     * @param lockerId 寄存柜ID
     * @return 柜格列表
     */
    List<LockerCell> listCellsByLockerId(Long lockerId);

    /**
     * 根据ID查询柜格。
     *
     * @param cellId 柜格ID
     * @return 柜格；不存在时返回null
     */
    LockerCell getCellById(Long cellId);

    /**
     * 管理员启用或停用柜格。
     *
     * @param cellId 柜格ID
     * @param status 目标状态
     * @return 是否修改成功
     */
    boolean changeCellStatus(Long cellId, CellStatus status);
}