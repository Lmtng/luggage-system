package com.luggage.luggagesystem.service;

import com.luggage.luggagesystem.entity.LockerCell;
import com.luggage.luggagesystem.enums.CellSizeType;
import com.luggage.luggagesystem.enums.CellStatus;

import java.util.List;

public interface LockerCellService {

    boolean occupyCell(Long cellId);

    boolean releaseCell(Long cellId);

    List<LockerCell> listAvailableCells(
            CellSizeType sizeType
    );

    LockerCell allocateAvailableCell(
            CellSizeType sizeType
    );

    LockerCell createCell(LockerCell lockerCell);

    List<LockerCell> listCellsByLockerId(
            Long lockerId
    );

    LockerCell getCellById(Long cellId);

    boolean changeCellStatus(
            Long cellId,
            CellStatus status
    );
}