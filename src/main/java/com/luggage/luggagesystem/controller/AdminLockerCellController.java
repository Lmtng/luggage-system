package com.luggage.luggagesystem.controller;

import com.luggage.luggagesystem.entity.LockerCell;
import com.luggage.luggagesystem.enums.CellStatus;
import com.luggage.luggagesystem.service.LockerCellService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/locker-cells")
public class AdminLockerCellController {

    private final LockerCellService lockerCellService;

    public AdminLockerCellController(
            LockerCellService lockerCellService) {

        this.lockerCellService = lockerCellService;
    }

    /**
     * 管理员新增柜格。
     */
    @PostMapping
    public ResponseEntity<LockerCell> createCell(
            @RequestBody LockerCell lockerCell) {

        LockerCell createdCell =
                lockerCellService.createCell(lockerCell);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdCell);
    }

    /**
     * 查询指定寄存柜下的全部柜格。
     */
    @GetMapping
    public List<LockerCell> listCells(
            @RequestParam Long lockerId) {

        return lockerCellService
                .listCellsByLockerId(lockerId);
    }

    /**
     * 根据ID查询柜格。
     */
    @GetMapping("/{cellId}")
    public ResponseEntity<LockerCell> getCell(
            @PathVariable Long cellId) {

        LockerCell lockerCell =
                lockerCellService.getCellById(cellId);

        if (lockerCell == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(lockerCell);
    }

    /**
     * 管理员启用或停用柜格。
     */
    @PutMapping("/{cellId}/status")
    public ResponseEntity<String> changeCellStatus(
            @PathVariable Long cellId,
            @RequestParam CellStatus status) {

        LockerCell existingCell =
                lockerCellService.getCellById(cellId);

        if (existingCell == null) {
            return ResponseEntity.notFound().build();
        }

        boolean success =
                lockerCellService.changeCellStatus(
                        cellId,
                        status
                );

        if (!success) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("柜格正在使用，不能修改状态");
        }

        return ResponseEntity.ok("柜格状态修改成功");
    }
}