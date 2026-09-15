package com.luggage.luggagesystem.controller;

import com.luggage.luggagesystem.dto.UserResponse;
import com.luggage.luggagesystem.entity.StorageOrder;
import com.luggage.luggagesystem.enums.UserStatus;
import com.luggage.luggagesystem.service.OperationLogService;
import com.luggage.luggagesystem.service.StorageOrderService;
import com.luggage.luggagesystem.service.UserService;
import com.luggage.luggagesystem.service.impl.LockerCellServiceImpl;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final StorageOrderService storageOrderService;
    private final LockerCellServiceImpl lockerCellService;
    private final OperationLogService operationLogService;


    /**
     * 查询全部用户。
     */
    @GetMapping
    public List<UserResponse> listUsers() {
        return userService.listUsers();
    }

    /**
     * 根据ID查询用户。
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(
            @PathVariable Long userId) {

        UserResponse user =
                userService.getUserById(userId);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(user);
    }

    /**
     * 禁用或恢复用户。
     */
    @PutMapping("/{userId}/status")
    public ResponseEntity<String> changeUserStatus(
            @PathVariable Long userId,
            @RequestParam UserStatus status,
            HttpSession session) {

        Object currentUserId = session.getAttribute(
                AuthController.LOGIN_USER_ID
        );

        // 防止管理员把当前登录的自己禁用
        if (status == UserStatus.DISABLED
                && userId.equals(currentUserId)) {
            throw new IllegalArgumentException(
                    "管理员不能禁用当前登录账号"
            );
        }

        // ✅ 先查询用户，确认存在
        var userResponse = userService.getUserById(userId);
        if (userResponse == null) {
            return ResponseEntity.notFound().build();
        }

        // ✅ 修改用户状态
        boolean success = userService.changeUserStatus(userId, status);
        if (!success) {
            return ResponseEntity.notFound().build();
        }

        // ✅ 如果是停用，释放该用户所有进行中的柜格
        int releasedCount = 0;
        if (status == UserStatus.DISABLED) {
            releasedCount = releaseUserActiveCells(userId, (Long) currentUserId);
        }

        // ✅ 记录操作日志
        String detail = "将用户 " + userResponse.getUsername() + " 状态改为 " + status.name();
        if (releasedCount > 0) {
            detail += "，同时释放 " + releasedCount + " 个柜格";
        }
        operationLogService.log(
                (Long) currentUserId,
                "CHANGE_USER_STATUS",
                "USER",
                userId,
                detail
        );

        // ✅ 返回结果
        String message = releasedCount > 0
                ? "用户已停用，其使用的 " + releasedCount + " 个柜格已释放"
                : "用户状态修改成功";

        return ResponseEntity.ok(message);
    }

    /**
     * 释放用户所有进行中的订单对应的柜格
     *
     * 处理逻辑：
     * 1. 查询用户所有 STORED 和 PENDING_PAYMENT 状态的订单
     * 2. 释放对应的柜格（OCCUPIED → AVAILABLE）
     * 3. 订单状态改为 EXCEPTION
     * 4. 记录操作日志
     *
     * @param userId 用户ID
     * @param adminId 操作管理员ID
     * @return 释放的柜格数量
     */
    private int releaseUserActiveCells(Long userId, Long adminId) {
        // 查询用户所有进行中的订单
        List<StorageOrder> activeOrders = storageOrderService.lambdaQuery()
                .eq(StorageOrder::getUserId, userId)
                .in(StorageOrder::getStatus,
                        StorageOrder.OrderStatus.STORED,
                        StorageOrder.OrderStatus.PENDING_PAYMENT)
                .list();

        if (activeOrders.isEmpty()) {
            log.info("用户 {} 没有进行中的订单，无需释放柜格", userId);
            return 0;
        }

        int releasedCount = 0;
        for (StorageOrder order : activeOrders) {
            try {
                // 释放柜格
                boolean released = lockerCellService.releaseCell(order.getCellId());
                if (!released) {
                    log.warn("释放柜格失败: cellId={}", order.getCellId());
                    continue;
                }

                // 订单状态改为 EXCEPTION
                order.setStatus(StorageOrder.OrderStatus.EXCEPTION);
                storageOrderService.updateById(order);
                releasedCount++;

                log.info("已释放柜格: cellId={}, orderId={}",
                        order.getCellId(), order.getId());

                // 记录日志
                operationLogService.log(
                        adminId,
                        "RELEASE_CELL_BY_USER_DISABLE",
                        "ORDER",
                        order.getId(),
                        "用户被停用，自动释放柜格 " + order.getCellId()
                );

            } catch (Exception e) {
                log.error("释放柜格异常: orderId={}", order.getId(), e);
            }
        }

        return releasedCount;
    }
}