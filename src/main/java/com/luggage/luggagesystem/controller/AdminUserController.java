package com.luggage.luggagesystem.controller;

import com.luggage.luggagesystem.dto.UserResponse;
import com.luggage.luggagesystem.enums.UserStatus;
import com.luggage.luggagesystem.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

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

        boolean success =
                userService.changeUserStatus(
                        userId,
                        status
                );

        if (!success) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok("用户状态修改成功");
    }
}