package com.luggage.luggagesystem.controller;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import com.luggage.luggagesystem.entity.LockerCell;
import com.luggage.luggagesystem.enums.CellSizeType;
import com.luggage.luggagesystem.service.LockerCellService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locker-cells")
public class LockerCellController {

    private final LockerCellService lockerCellService;

    public LockerCellController(LockerCellService lockerCellService) {
        this.lockerCellService = lockerCellService;
    }

    @GetMapping("/available")
    public List<LockerCell> listAvailableCells(
            @RequestParam(required = false) CellSizeType sizeType) {
        return lockerCellService.listAvailableCells(sizeType);
    }
    @PostMapping("/{cellId}/occupy")
    public ResponseEntity<String> occupyCell(@PathVariable Long cellId) {
        boolean success = lockerCellService.occupyCell(cellId);

        if (success) {
            return ResponseEntity.ok("柜格占用成功");
        }

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body("柜格不存在或当前不是可用状态");
    }

    @PostMapping("/{cellId}/release")
    public ResponseEntity<String> releaseCell(@PathVariable Long cellId) {
        boolean success = lockerCellService.releaseCell(cellId);

        if (success) {
            return ResponseEntity.ok("柜格释放成功");
        }

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body("柜格不存在或当前不是占用状态");
    }

}