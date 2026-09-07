package com.luggage.luggagesystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luggage.luggagesystem.dto.CreateOrderRequest;
import com.luggage.luggagesystem.dto.CreateOrderResponse;
import com.luggage.luggagesystem.dto.PickupVerifyRequest;
import com.luggage.luggagesystem.dto.PickupVerifyResponse;
import com.luggage.luggagesystem.entity.LockerCell;
import com.luggage.luggagesystem.entity.PriceRule;
import com.luggage.luggagesystem.entity.StorageOrder;
import com.luggage.luggagesystem.enums.CellStatus;
import com.luggage.luggagesystem.exception.BusinessException;
import com.luggage.luggagesystem.mapper.StorageOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 寄存订单业务服务。
 *
 * 主要功能：
 * 1. 创建寄存订单
 * 2. 查询用户订单
 * 3. 验证取件码并计算费用
 * 4. 完成取件
 * 5. 管理员查询和处理异常订单
 * 6. 订单统计
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StorageOrderService
        extends ServiceImpl<StorageOrderMapper, StorageOrder> {

    /**
     * 取件码最大允许错误次数。
     */
    private static final int MAX_PICKUP_ERRORS = 5;

    /**
     * 保存每个订单取件码输入错误的次数。
     */
    private final Map<Long, Integer> errorCountMap =
            new ConcurrentHashMap<>();

    private final LockerCellService lockerCellService;
    private final PriceRuleService priceRuleService;
    private final PickupCodeGenerator pickupCodeGenerator;
    private final OrderNoGenerator orderNoGenerator;

    /**
     * 创建寄存订单。
     *
     * userId 和 cellId 都从 request 中取得，
     * 与当前 OrderController 和测试代码保持一致。
     */
    @Transactional
    public CreateOrderResponse createOrder(
            CreateOrderRequest request) {

        if (request == null) {
            throw new BusinessException("创建订单参数不能为空");
        }

        Long userId = request.getUserId();
        Long cellId = request.getCellId();

        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        if (cellId == null) {
            throw new BusinessException("请选择柜格");
        }

        LockerCell cell = lockerCellService.getCellById(cellId);

        if (cell == null) {
            throw new BusinessException("柜格不存在");
        }

        if (!CellStatus.AVAILABLE.equals(cell.getStatus())) {
            throw new BusinessException("柜格当前不可用");
        }

        /*
         * 使用带状态条件的更新语句占用柜格。
         * 只有柜格当前为 AVAILABLE 时才会成功，
         * 可以防止两个用户同时占用同一个柜格。
         */
        boolean occupied =
                lockerCellService.occupyCell(cellId);

        if (!occupied) {
            throw new BusinessException(
                    "柜格已被其他用户占用，请重新选择"
            );
        }

        String orderNo =
                orderNoGenerator.generateOrderNo();

        String pickupCode =
                pickupCodeGenerator.generateCode();

        String pickupCodeHash =
                pickupCodeGenerator.encryptCode(pickupCode);

        StorageOrder order = new StorageOrder();

        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setCellId(cellId);
        order.setPickupCodeHash(pickupCodeHash);
        order.setStartTime(LocalDateTime.now());
        order.setEndTime(null);
        order.setAmount(BigDecimal.ZERO);
        order.setPaymentStatus(
                StorageOrder.PaymentStatus.UNPAID
        );
        order.setStatus(
                StorageOrder.OrderStatus.STORED
        );

        boolean saved = this.save(order);

        if (!saved) {
            /*
             * 如果订单保存失败，释放刚刚占用的柜格。
             */
            lockerCellService.releaseCell(cellId);

            throw new BusinessException("创建寄存订单失败");
        }

        String message =
                "寄存成功！请妥善保管取件码："
                        + pickupCode;

        CreateOrderResponse response =
                new CreateOrderResponse();

        response.setOrderId(order.getId());
        response.setOrderNo(order.getOrderNo());
        response.setPickupCode(pickupCode);
        response.setCellNo(cell.getCellNo());
        response.setStartTime(order.getStartTime());
        response.setStatus(order.getStatus());
        response.setMessage(message);

        log.info(
                "订单创建成功，orderId={}，orderNo={}，cellId={}",
                order.getId(),
                order.getOrderNo(),
                cellId
        );

        return response;
    }

    /**
     * 查询当前用户的订单列表。
     */
    public Page<StorageOrder> getMyOrders(
            Long userId,
            long current,
            long size) {

        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        if (current <= 0) {
            current = 1;
        }

        if (size <= 0) {
            size = 10;
        }

        LambdaQueryWrapper<StorageOrder> wrapper =
                new LambdaQueryWrapper<>();

        wrapper.eq(StorageOrder::getUserId, userId)
                .orderByDesc(StorageOrder::getCreatedAt);

        return this.page(
                new Page<>(current, size),
                wrapper
        );
    }

    /**
     * 查询订单详情。
     *
     * 参数顺序与当前 Controller 和测试代码一致：
     * 第一个参数是 orderId，第二个参数是 userId。
     */
    public StorageOrder getOrderDetail(
            Long orderId,
            Long userId) {

        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        if (orderId == null) {
            throw new BusinessException("订单ID不能为空");
        }

        StorageOrder order = this.getById(orderId);

        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (!userId.equals(order.getUserId())) {
            throw new BusinessException("无权查看该订单");
        }

        return order;
    }

    /**
     * 验证取件码并计算费用。
     *
     * orderId、userId 和 pickupCode 都从 request 中取得。
     */
    @Transactional
    public PickupVerifyResponse verifyPickup(
            PickupVerifyRequest request) {

        if (request == null) {
            throw new BusinessException("取件验证参数不能为空");
        }

        Long userId = request.getUserId();
        Long orderId = request.getOrderId();
        String pickupCode = request.getPickupCode();

        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        if (orderId == null) {
            throw new BusinessException("订单ID不能为空");
        }

        if (pickupCode == null || pickupCode.isBlank()) {
            throw new BusinessException("请输入取件码");
        }

        StorageOrder order = this.getById(orderId);

        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (!userId.equals(order.getUserId())) {
            throw new BusinessException("无权操作该订单");
        }

        if (StorageOrder.OrderStatus.COMPLETED.equals(
                order.getStatus())) {

            throw new BusinessException(
                    "该订单已完成，不能重复取件"
            );
        }

        if (!StorageOrder.OrderStatus.STORED.equals(
                order.getStatus())) {

            throw new BusinessException(
                    "当前订单状态不能取件"
            );
        }

        boolean codeMatched =
                pickupCodeGenerator.verifyCode(
                        pickupCode,
                        order.getPickupCodeHash()
                );

        if (!codeMatched) {
            int errorCount = errorCountMap.merge(
                    orderId,
                    1,
                    Integer::sum
            );

            log.warn(
                    "取件码验证失败，orderId={}，错误次数={}",
                    orderId,
                    errorCount
            );

            if (errorCount >= MAX_PICKUP_ERRORS) {
                throw new BusinessException(
                        "取件码错误次数过多，请联系管理员"
                );
            }

            throw new BusinessException(
                    "取件码错误，请重新输入"
            );
        }

        /*
         * 取件码验证成功，清除错误次数。
         */
        errorCountMap.remove(orderId);

        LockerCell cell =
                lockerCellService.getCellById(
                        order.getCellId()
                );

        if (cell == null) {
            throw new BusinessException(
                    "订单对应的柜格不存在"
            );
        }

        if (cell.getSizeType() == null) {
            throw new BusinessException(
                    "柜格规格信息异常"
            );
        }

        String sizeType =
                cell.getSizeType().name();

        LocalDateTime currentTime =
                LocalDateTime.now();

        PriceRule rule =
                priceRuleService
                        .getEnabledRuleBySizeType(sizeType);

        if (rule == null) {
            throw new BusinessException(
                    "未找到该柜格规格的有效计费规则"
            );
        }

        BigDecimal amount =
                priceRuleService.calculateFee(
                        sizeType,
                        order.getStartTime(),
                        currentTime
                );

        if (amount == null) {
            throw new BusinessException(
                    "费用计算失败"
            );
        }

        long actualMinutes = Math.max(
                0,
                ChronoUnit.MINUTES.between(
                        order.getStartTime(),
                        currentTime
                )
        );

        int freeMinutes =
                rule.getFreeMinutes() == null
                        ? 0
                        : rule.getFreeMinutes();

        int unitMinutes =
                rule.getUnitMinutes() == null
                        || rule.getUnitMinutes() <= 0
                        ? 60
                        : rule.getUnitMinutes();

        long chargeableMinutes = Math.max(
                0,
                actualMinutes - freeMinutes
        );

        order.setAmount(amount);
        order.setStatus(
                StorageOrder.OrderStatus.PENDING_PAYMENT
        );

        boolean updated = this.updateById(order);

        if (!updated) {
            throw new BusinessException(
                    "更新订单计费信息失败"
            );
        }

        String message =
                "取件码验证成功！请确认支付 "
                        + amount
                        + " 元";

        PickupVerifyResponse response =
                new PickupVerifyResponse();

        response.setOrderId(order.getId());
        response.setOrderNo(order.getOrderNo());
        response.setCellNo(cell.getCellNo());
        response.setStartTime(order.getStartTime());
        response.setCurrentTime(currentTime);
        response.setActualMinutes(actualMinutes);
        response.setFreeMinutes(freeMinutes);
        response.setChargeableMinutes(
                chargeableMinutes
        );
        response.setUnitMinutes(unitMinutes);
        response.setUnitPrice(rule.getUnitPrice());
        response.setCapAmount(rule.getCapAmount());
        response.setAmount(amount);
        response.setMessage(message);

        log.info(
                "取件码验证成功，orderId={}，amount={}",
                orderId,
                amount
        );

        return response;
    }

    /**
     * 完成取件。
     *
     * 参数顺序与 Controller 和测试代码一致：
     * 第一个参数是 orderId，第二个参数是 userId。
     */
    @Transactional
    public void completeOrder(
            Long orderId,
            Long userId) {

        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        if (orderId == null) {
            throw new BusinessException("订单ID不能为空");
        }

        StorageOrder order = this.getById(orderId);

        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (!userId.equals(order.getUserId())) {
            throw new BusinessException("无权操作该订单");
        }

        if (StorageOrder.OrderStatus.COMPLETED.equals(
                order.getStatus())) {

            throw new BusinessException(
                    "该订单已经完成"
            );
        }

        if (!StorageOrder.OrderStatus.PENDING_PAYMENT.equals(
                order.getStatus())) {

            throw new BusinessException(
                    "当前订单状态不能完成取件"
            );
        }

        /*
         * 先释放柜格。
         *
         * 如果后面的订单更新失败，因为当前方法带事务，
         * 柜格释放操作也会回滚。
         */
        boolean released =
                lockerCellService.releaseCell(
                        order.getCellId()
                );

        if (!released) {
            throw new BusinessException(
                    "释放柜格失败，请联系管理员处理"
            );
        }

        order.setPaymentStatus(
                StorageOrder.PaymentStatus.PAID
        );
        order.setStatus(
                StorageOrder.OrderStatus.COMPLETED
        );
        order.setEndTime(LocalDateTime.now());

        boolean updated = this.updateById(order);

        if (!updated) {
            throw new BusinessException(
                    "更新订单状态失败"
            );
        }

        errorCountMap.remove(orderId);

        log.info(
                "订单完成，orderId={}，cellId={}",
                orderId,
                order.getCellId()
        );
    }

    /**
     * 管理员分页查询订单。
     */
    public Page<StorageOrder> adminGetOrders(
            long current,
            long size,
            String status) {

        if (current <= 0) {
            current = 1;
        }

        if (size <= 0) {
            size = 10;
        }

        LambdaQueryWrapper<StorageOrder> wrapper =
                new LambdaQueryWrapper<>();

        if (status != null && !status.isBlank()) {
            wrapper.eq(
                    StorageOrder::getStatus,
                    status.trim().toUpperCase()
            );
        }

        wrapper.orderByDesc(
                StorageOrder::getCreatedAt
        );

        return this.page(
                new Page<>(current, size),
                wrapper
        );
    }

    /**
     * 管理员处理异常订单。
     */
    @Transactional
    public void fixExceptionOrder(
            Long orderId,
            String targetStatus,
            Long adminId) {

        if (adminId == null) {
            throw new BusinessException("管理员未登录");
        }

        if (orderId == null) {
            throw new BusinessException("订单ID不能为空");
        }

        if (targetStatus == null
                || targetStatus.isBlank()) {

            throw new BusinessException(
                    "目标状态不能为空"
            );
        }

        String normalizedStatus =
                targetStatus.trim().toUpperCase();

        boolean validTarget =
                StorageOrder.OrderStatus.STORED.equals(
                        normalizedStatus)
                        || StorageOrder.OrderStatus.COMPLETED.equals(
                        normalizedStatus)
                        || StorageOrder.OrderStatus.CANCELLED.equals(
                        normalizedStatus);

        if (!validTarget) {
            throw new BusinessException(
                    "目标状态不合法"
            );
        }

        StorageOrder order = this.getById(orderId);

        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (!StorageOrder.OrderStatus.EXCEPTION.equals(
                order.getStatus())) {

            throw new BusinessException(
                    "只有异常状态的订单才能处理"
            );
        }

        /*
         * 修复为已完成或已取消时，应当释放柜格。
         *
         * 异常订单的柜格可能已经由管理员人工释放，
         * 因此释放失败时只记录日志，不阻止订单修复。
         */
        if (StorageOrder.OrderStatus.COMPLETED.equals(
                normalizedStatus)
                || StorageOrder.OrderStatus.CANCELLED.equals(
                normalizedStatus)) {

            boolean released =
                    lockerCellService.releaseCell(
                            order.getCellId()
                    );

            if (!released) {
                log.warn(
                        "异常订单对应柜格未释放，" +
                                "柜格可能已经处于可用状态，" +
                                "orderId={}，cellId={}",
                        orderId,
                        order.getCellId()
                );
            }
        }

        if (StorageOrder.OrderStatus.COMPLETED.equals(
                normalizedStatus)) {

            order.setEndTime(LocalDateTime.now());
        }

        order.setStatus(normalizedStatus);

        boolean updated = this.updateById(order);

        if (!updated) {
            throw new BusinessException(
                    "异常订单处理失败"
            );
        }

        errorCountMap.remove(orderId);

        log.info(
                "管理员处理异常订单，" +
                        "adminId={}，orderId={}，targetStatus={}",
                adminId,
                orderId,
                normalizedStatus
        );
    }

    /**
     * 获取订单统计信息。
     */
    public Map<String, Object> getStatistics() {

        List<StorageOrder> orders = this.list();

        long totalOrders = orders.size();

        long storedOrders = orders.stream()
                .filter(order ->
                        StorageOrder.OrderStatus.STORED.equals(
                                order.getStatus()))
                .count();

        long pendingPaymentOrders = orders.stream()
                .filter(order ->
                        StorageOrder.OrderStatus.PENDING_PAYMENT.equals(
                                order.getStatus()))
                .count();

        long completedOrders = orders.stream()
                .filter(order ->
                        StorageOrder.OrderStatus.COMPLETED.equals(
                                order.getStatus()))
                .count();

        long cancelledOrders = orders.stream()
                .filter(order ->
                        StorageOrder.OrderStatus.CANCELLED.equals(
                                order.getStatus()))
                .count();

        long exceptionOrders = orders.stream()
                .filter(order ->
                        StorageOrder.OrderStatus.EXCEPTION.equals(
                                order.getStatus()))
                .count();

        BigDecimal totalRevenue = orders.stream()
                .filter(order ->
                        StorageOrder.OrderStatus.COMPLETED.equals(
                                order.getStatus()))
                .map(order ->
                        order.getAmount() == null
                                ? BigDecimal.ZERO
                                : order.getAmount())
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        Map<String, Object> statistics =
                new HashMap<>();

        statistics.put("totalOrders", totalOrders);
        statistics.put("storedOrders", storedOrders);
        statistics.put(
                "pendingPaymentOrders",
                pendingPaymentOrders
        );
        statistics.put(
                "completedOrders",
                completedOrders
        );
        statistics.put(
                "cancelledOrders",
                cancelledOrders
        );
        statistics.put(
                "exceptionOrders",
                exceptionOrders
        );
        statistics.put(
                "totalRevenue",
                totalRevenue
        );

        /*
         * 同时提供按状态命名的统计字段，
         * 方便管理员前端直接显示。
         */
        statistics.put(
                "status_STORED",
                storedOrders
        );
        statistics.put(
                "status_PENDING_PAYMENT",
                pendingPaymentOrders
        );
        statistics.put(
                "status_COMPLETED",
                completedOrders
        );
        statistics.put(
                "status_CANCELLED",
                cancelledOrders
        );
        statistics.put(
                "status_EXCEPTION",
                exceptionOrders
        );

        return statistics;
    }
}
