package com.luggage.luggagesystem.controller;

import com.luggage.luggagesystem.entity.Locker;
import com.luggage.luggagesystem.enums.LockerStatus;
import com.luggage.luggagesystem.service.LockerService;
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
@RequestMapping("/api/admin/lockers")
public class LockerController {

    private final LockerService lockerService;

    public LockerController(LockerService lockerService) {
        this.lockerService = lockerService;
    }

    @PostMapping
    public ResponseEntity<Locker> createLocker(
            @RequestBody Locker locker) {

        Locker createdLocker = lockerService.createLocker(locker);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdLocker);
    }

    @GetMapping
    public List<Locker> listLockers() {
        return lockerService.listLockers();
    }

    @GetMapping("/{lockerId}")
    public ResponseEntity<Locker> getLocker(
            @PathVariable Long lockerId) {

        Locker locker = lockerService.getLockerById(lockerId);

        if (locker == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(locker);
    }

    @PutMapping("/{lockerId}/status")
    public ResponseEntity<String> changeLockerStatus(
            @PathVariable Long lockerId,
            @RequestParam LockerStatus status) {

        boolean success =
                lockerService.changeLockerStatus(lockerId, status);

        if (!success) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok("寄存柜状态修改成功");
    }
}