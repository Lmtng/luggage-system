package com.luggage.luggagesystem.controller;

import com.luggage.luggagesystem.common.AuthContext;
import com.luggage.luggagesystem.common.Result;
import com.luggage.luggagesystem.dto.CreateOrderRequest;
import com.luggage.luggagesystem.dto.CreateOrderResponse;
import com.luggage.luggagesystem.dto.PickupVerifyRequest;
import com.luggage.luggagesystem.dto.PickupVerifyResponse;
import com.luggage.luggagesystem.entity.StorageOrder;
import com.luggage.luggagesystem.exception.BusinessException;
import com.luggage.luggagesystem.service.StorageOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 订单控制器（用户端）
 * 功能：
 * 1. 创建寄存订单
 * 2. 查询个人订单列表
 * 3. 查询订单详情
 * 4. 验证取件码
 * 5. 完成取件
 * 接口路径：
 * - POST   /api/orders                 创建订单
 * - GET    /api/orders/my              个人订单列表
 * - GET    /api/orders/{id}            订单详情
 * - POST   /api/orders/{id}/pickup/verify  验证取件码
 * - POST   /api/orders/{id}/complete   完成取件
 *
 * @author 成员B
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final StorageOrderService storageOrderService;

    /**
     * 创建寄存订单
     * <p>
     * 请求体：
     * {
     * "cellId": 1
     * }
     * 响应：
     * {
     * "code": 200,
     * "message": "寄存成功！",
     * "data": {
     * "orderId": 1,
     * "orderNo": "ST202609010001",
     * "pickupCode": "690373",
     * "cellNo": "A-01",
     * "startTime": "2026-09-01T18:20:07",
     * "status": "STORED",
     * "message": "寄存成功！请妥善保管取件码：690373"
     * }
     * }
     */
    @PostMapping
    public Result<CreateOrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        log.info("创建订单请求: cellId={}", request.getCellId());

        try {
            // TODO: 从登录凭证获取当前用户ID，而不是由前端传入
            Long userId = AuthContext.getCurrentUserId();
            if (userId == null) {
                return Result.error(1001, "用户未登录");
            }
            request.setUserId(userId);

            // 临时：使用测试用户
            // request.setUserId(100L);

            CreateOrderResponse response = storageOrderService.createOrder(request);
            return Result.success("寄存成功！请妥善保管取件码：" + response.getPickupCode(), response);

        } catch (BusinessException e) {
            log.warn("创建订单失败: {}", e.getMessage());
            return Result.error(1001, e.getMessage());
        } catch (Exception e) {
            log.error("创建订单异常", e);
            return Result.error(500, "创建订单失败，请重试");
        }
    }

    /**
     * 查询个人订单列表（分页）
     * <p>
     * 请求参数：
     * - page: 页码，默认1
     * - size: 每页大小，默认10
     * <p>
     * 响应：
     * {
     * "code": 200,
     * "message": "操作成功",
     * "data": {
     * "records": [...],
     * "total": 10,
     * "size": 10,
     * "current": 1,
     * "pages": 1
     * }
     * }
     */
    @GetMapping("/my")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<StorageOrder>> getMyOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("查询个人订单列表: page={}, size={}", page, size);

        try {
            // TODO: 从登录凭证获取当前用户ID
            Long userId = AuthContext.getCurrentUserId();
            if (userId == null) {
                return Result.error(1001, "用户未登录");
            }

            var result = storageOrderService.getMyOrders(userId, page, size);
            return Result.success(result);

        } catch (BusinessException e) {
            log.warn("查询订单列表失败: {}", e.getMessage());
            return Result.error(1001, e.getMessage());
        } catch (Exception e) {
            log.error("查询订单列表异常", e);
            return Result.error(500, "查询失败，请重试");
        }
    }

    /**
     * 查询订单详情
     * <p>
     * 响应：
     * {
     * "code": 200,
     * "message": "操作成功",
     * "data": {
     * "id": 1,
     * "orderNo": "ST202609010001",
     * "userId": 100,
     * "cellId": 1,
     * "status": "STORED",
     * ...
     * }
     * }
     */
    @GetMapping("/{id}")
    public Result<StorageOrder> getOrderDetail(@PathVariable Long id) {
        log.info("查询订单详情: id={}", id);

        try {
            // TODO: 从登录凭证获取当前用户ID
            Long userId = AuthContext.getCurrentUserId();
            if (userId == null) {
                return Result.error(1001, "用户未登录");
            }

            StorageOrder order = storageOrderService.getOrderDetail(id, userId);
            return Result.success(order);

        } catch (BusinessException e) {
            log.warn("查询订单详情失败: {}", e.getMessage());
            return Result.error(1001, e.getMessage());
        } catch (Exception e) {
            log.error("查询订单详情异常", e);
            return Result.error(500, "查询失败，请重试");
        }
    }

    /**
     * 验证取件码并计算费用
     * <p>
     * 请求体：
     * {
     * "orderId": 1,
     * "pickupCode": "690373"
     * }
     * <p>
     * 响应：
     * {
     * "code": 200,
     * "message": "取件码验证成功！请确认支付 2.00 元",
     * "data": {
     * "orderId": 1,
     * "orderNo": "ST202609010001",
     * "amount": 2.00,
     * "actualMinutes": 120,
     * ...
     * }
     * }
     */
    @PostMapping("/{id}/pickup/verify")
    public Result<PickupVerifyResponse> verifyPickup(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody) {
        log.info("验证取件码: orderId={}", id);

        try {
            // TODO: 从登录凭证获取当前用户ID
            Long userId = AuthContext.getCurrentUserId();
            if (userId == null) {
                return Result.error(1001, "用户未登录");
            }

            String pickupCode = requestBody.get("pickupCode");
            if (pickupCode == null || pickupCode.isEmpty()) {
                return Result.error(1001, "请输入取件码");
            }

            PickupVerifyRequest request = new PickupVerifyRequest();
            request.setOrderId(id);
            request.setUserId(userId);
            request.setPickupCode(pickupCode);

            PickupVerifyResponse response = storageOrderService.verifyPickup(request);
            return Result.success(response.getMessage(), response);

        } catch (BusinessException e) {
            log.warn("验证取件码失败: {}", e.getMessage());
            return Result.error(1001, e.getMessage());
        } catch (Exception e) {
            log.error("验证取件码异常", e);
            return Result.error(500, "验证失败，请重试");
        }
    }

    /**
     * 完成取件（模拟支付）
     * <p>
     * 响应：
     * {
     * "code": 200,
     * "message": "取件成功！",
     * "data": null
     * }
     */
    @PostMapping("/{id}/complete")
    public Result<String> completeOrder(@PathVariable Long id) {
        log.info("完成取件: orderId={}", id);

        try {
            // TODO: 从登录凭证获取当前用户ID
            Long userId = AuthContext.getCurrentUserId();
            if (userId == null) {
                return Result.error(1001, "用户未登录");
            }

            storageOrderService.completeOrder(id, userId);
            return Result.success("取件成功！");

        } catch (BusinessException e) {
            log.warn("完成取件失败: {}", e.getMessage());
            return Result.error(1001, e.getMessage());
        } catch (Exception e) {
            log.error("完成取件异常", e);
            return Result.error(500, "取件失败，请重试");
        }
    }
}