package com.luggage.luggagesystem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luggage.luggagesystem.common.Result;
import com.luggage.luggagesystem.entity.PriceRule;
import com.luggage.luggagesystem.entity.StorageOrder;
import com.luggage.luggagesystem.exception.BusinessException;
import com.luggage.luggagesystem.service.PriceRuleService;
import com.luggage.luggagesystem.service.StorageOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员控制器
 *
 * 功能：
 * 1. 查询所有订单（分页）
 * 2. 修改计费规则
 * 3. 处理异常订单
 * 4. 获取统计数据
 *
 * 接口路径（全部以 /api/admin 开头）：
 * - GET    /api/admin/orders              查询所有订单
 * - PUT    /api/admin/price-rules/{id}    修改计费规则
 * - PUT    /api/admin/orders/{id}/status  处理异常订单
 * - GET    /api/admin/statistics          获取统计数据
 *
 * 权限要求：ADMIN 角色
 *
 * @author 成员B
 * @date 2026-09-01
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final StorageOrderService storageOrderService;
    private final PriceRuleService priceRuleService;

    /**
     * 管理员查询所有订单（分页）
     *
     * 请求参数：
     * - page: 页码，默认1
     * - size: 每页大小，默认10
     * - status: 订单状态（可选，如：STORED、COMPLETED）
     *
     * 响应：
     * {
     *   "code": 200,
     *   "message": "操作成功",
     *   "data": {
     *     "records": [...],
     *     "total": 100,
     *     "size": 10,
     *     "current": 1,
     *     "pages": 10
     *   }
     * }
     */
    @GetMapping("/orders")
    public Result<Page<StorageOrder>> getOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        log.info("管理员查询订单: page={}, size={}, status={}", page, size, status);

        try {
            // TODO: 校验当前用户是否为管理员
            // 在拦截器中已经校验，这里可以省略

            Page<StorageOrder> result = storageOrderService.adminGetOrders(page, size, status);
            return Result.success(result);

        } catch (BusinessException e) {
            log.warn("管理员查询订单失败: {}", e.getMessage());
            return Result.error(1001, e.getMessage());
        } catch (Exception e) {
            log.error("管理员查询订单异常", e);
            return Result.error(500, "查询失败，请重试");
        }
    }

    /**
     * 管理员修改计费规则
     *
     * 请求体：
     * {
     *   "unitPrice": 2.50,
     *   "freeMinutes": 20,
     *   "unitMinutes": 60,
     *   "capAmount": 25.00,
     *   "enabled": 1
     * }
     *
     * 响应：
     * {
     *   "code": 200,
     *   "message": "修改成功",
     *   "data": null
     * }
     */
    @PutMapping("/price-rules/{id}")
    public Result<String> updatePriceRule(
            @PathVariable Long id,
            @RequestBody PriceRule rule) {
        log.info("管理员修改计费规则: id={}", id);

        try {
            // 设置ID，确保更新的是正确的规则
            rule.setId(id);

            boolean success = priceRuleService.updateRule(rule);
            if (success) {
                // TODO: 记录操作日志
                // operationLogService.log(adminId, "UPDATE_PRICE_RULE", "PRICE_RULE", id, "修改了计费规则");
                return Result.success("计费规则修改成功");
            } else {
                return Result.error(1001, "修改失败，请重试");
            }

        } catch (BusinessException e) {
            log.warn("修改计费规则失败: {}", e.getMessage());
            return Result.error(1001, e.getMessage());
        } catch (Exception e) {
            log.error("修改计费规则异常", e);
            return Result.error(500, "修改失败，请重试");
        }
    }

    /**
     * 管理员处理异常订单
     * 请求体：
     * {
     *   "targetStatus": "COMPLETED"
     * }
     * 响应：
     * {
     *   "code": 200,
     *   "message": "异常订单处理成功",
     *   "data": null
     * }
     */
    @PutMapping("/orders/{id}/status")
    public Result<String> fixExceptionOrder(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody) {
        log.info("管理员处理异常订单: orderId={}", id);

        try {
            // TODO: 从登录凭证获取管理员ID
            Long adminId = 1L;

            String targetStatus = requestBody.get("targetStatus");
            if (targetStatus == null || targetStatus.isEmpty()) {
                return Result.error(1001, "请指定目标状态");
            }

            storageOrderService.fixExceptionOrder(id, targetStatus, adminId);

            // TODO: 记录操作日志
            // operationLogService.log(adminId, "FIX_EXCEPTION_ORDER", "ORDER", id,
            //         "将异常订单调整为: " + targetStatus);

            return Result.success("异常订单处理成功");

        } catch (BusinessException e) {
            log.warn("处理异常订单失败: {}", e.getMessage());
            return Result.error(1001, e.getMessage());
        } catch (Exception e) {
            log.error("处理异常订单异常", e);
            return Result.error(500, "处理失败，请重试");
        }
    }

    /**
     * 获取统计数据
     * 响应：
     * {
     *   "code": 200,
     *   "message": "操作成功",
     *   "data": {
     *     "totalOrders": 100,
     *     "status_STORED": 30,
     *     "status_COMPLETED": 60,
     *     "totalRevenue": 200.00
     *   }
     * }
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        log.info("管理员获取统计数据");

        try {
            Map<String, Object> stats = storageOrderService.getStatistics();
            return Result.success(stats);

        } catch (Exception e) {
            log.error("获取统计数据异常", e);
            return Result.error(500, "获取统计数据失败，请重试");
        }
    }

    /**
     * 获取所有计费规则（管理员查看）
     */
    @GetMapping("/price-rules")
    public Result<java.util.List<PriceRule>> getAllPriceRules() {
        log.info("管理员获取所有计费规则");

        try {
            java.util.List<PriceRule> rules = priceRuleService.getAllRules();
            return Result.success(rules);

        } catch (Exception e) {
            log.error("获取计费规则异常", e);
            return Result.error(500, "获取失败，请重试");
        }
    }
}