package com.luggage.luggagesystem.controller;

import com.luggage.luggagesystem.common.Result;
import com.luggage.luggagesystem.dto.CreateOrderRequest;
import com.luggage.luggagesystem.dto.OrderVO;
import com.luggage.luggagesystem.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 临时模拟：从请求头获取用户ID（后面会换成真正的登录认证）
    private Long getCurrentUserId() {
        return 1L;  // 暂时写死，用管理员账号测试
    }

    @PostMapping
    public Result<OrderVO> createOrder(@RequestBody CreateOrderRequest request) {
        Long userId = getCurrentUserId();
        OrderVO order = orderService.createOrder(userId, request);
        return Result.success(order);
    }
}