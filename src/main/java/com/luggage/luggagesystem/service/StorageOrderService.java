package com.luggage.luggagesystem.service;

import com.luggage.luggagesystem.entity.LockerCell;
import com.luggage.luggagesystem.entity.StorageOrder;
import com.luggage.luggagesystem.enums.CellSizeType;
import com.luggage.luggagesystem.enums.CellStatus;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luggage.luggagesystem.dto.*;
import com.luggage.luggagesystem.entity.PriceRule;
import com.luggage.luggagesystem.entity.StorageOrder;
import com.luggage.luggagesystem.exception.BusinessException;
import com.luggage.luggagesystem.mapper.StorageOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 寄存订单服务类
 *
 * 功能说明：
 * 1. 创建寄存订单（占用柜格、生成订单号、生成取件码）
 * 2. 查询个人订单列表（分页）
 * 3. 查询订单详情
 * 4. 验证取件码并计算费用
 * 5. 完成取件（模拟支付、释放柜格）
 * 6. 管理员查询所有订单（分页）
 * 7. 管理员处理异常订单
 *
 * 核心业务规则：
 * - 创建订单时，必须在事务中占用柜格，失败则回滚
 * - 完成取件时，必须在事务中释放柜格，失败则回滚
 * - 取件码验证失败达到阈值时，需要限制
 *
 * @author 成员B
 * @date 2026-09-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StorageOrderService extends ServiceImpl<StorageOrderMapper, StorageOrder> {

    private final PickupCodeGenerator pickupCodeGenerator;
    private final OrderNoGenerator orderNoGenerator;
    private final PriceRuleService priceRuleService;
    private final LockerCellService lockerCellService;
    // ========== 常量 ==========

    /**
     * 取件码连续错误最大次数
     */
    private static final int MAX_PICKUP_ERRORS = 5;

    /**
     * 错误计数缓存（实际项目可用Redis）
     */
    private final java.util.Map<Long, Integer> errorCountMap = new java.util.concurrent.ConcurrentHashMap<>();

    // ========== 核心业务方法 ==========

    /**
     * 创建寄存订单
     *
     * 业务流程：
     * 1. 校验柜格是否可用（调用成员A的接口，这里先模拟）
     * 2. 占用柜格（条件更新，防止并发）
     * 3. 生成订单号
     * 4. 生成取件码并加密
     * 5. 保存订单
     * 6. 返回取件码给用户
     * @param request 创建订单请求
     * @return 创建订单响应（包含取件码）
     */
    @Transactional(rollbackFor = Exception.class)
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        log.info("开始创建订单, userId={}, cellId={}", request.getUserId(), request.getCellId());

        // 1. 校验参数
        if (request.getUserId() == null) {
            throw new BusinessException("用户未登录");
        }
        if (request.getCellId() == null) {
            throw new BusinessException("请选择柜格");
        }

        // 2. ✅ 获取柜格信息，校验是否可用
        LockerCell cell = lockerCellService.getCellById(request.getCellId());
        if (cell == null) {
            throw new BusinessException("柜格不存在");
        }
        if (cell.getStatus() != CellStatus.AVAILABLE) {
            throw new BusinessException(BusinessException.CELL_OCCUPIED, "柜格不可用，请重新选择");
        }

        // 3. ✅ 占用柜格（将状态改为 OCCUPIED）
        boolean occupied = lockerCellService.changeCellStatus(request.getCellId(), CellStatus.OCCUPIED);
        if (!occupied) {
            throw new BusinessException(BusinessException.CELL_OCCUPIED, "柜格已被占用，请重新选择");
        }
        log.info("柜格占用成功, cellId={}", request.getCellId());

        // 4. 生成订单号
        String orderNo = orderNoGenerator.generateOrderNo();
        log.info("生成订单号: {}", orderNo);

        // 5. 生成取件码
        String pickupCode = pickupCodeGenerator.generateCode();
        String pickupCodeHash = pickupCodeGenerator.encryptCode(pickupCode);
        log.info("生成取件码: {}", pickupCode);

        // 6. 构建订单实体
        StorageOrder order = new StorageOrder();
        order.setOrderNo(orderNo);
        order.setUserId(request.getUserId());
        order.setCellId(request.getCellId());
        order.setPickupCodeHash(pickupCodeHash);
        order.setStartTime(LocalDateTime.now());
        order.setAmount(BigDecimal.ZERO);
        order.setPaymentStatus(StorageOrder.PaymentStatus.UNPAID);
        order.setStatus(StorageOrder.OrderStatus.STORED);

        // 7. 保存订单
        boolean saved = this.save(order);
        if (!saved) {
            // 保存失败，释放柜格
            lockerCellService.changeCellStatus(request.getCellId(), CellStatus.AVAILABLE);
            throw new BusinessException("创建订单失败，请重试");
        }

        log.info("订单创建成功, orderId={}, orderNo={}", order.getId(), order.getOrderNo());

        // 8. ✅ 获取柜格编号
        String cellNo = cell.getCellNo();

        return CreateOrderResponse.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .pickupCode(pickupCode)
                .cellNo(cellNo)
                .startTime(order.getStartTime())
                .status(order.getStatus())
                .message("寄存成功！请妥善保管取件码：" + pickupCode)
                .build();
    }

    /**
     * 查询个人订单列表（分页）
     *
     * @param userId 用户ID
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @return 订单分页数据
     */
    public Page<StorageOrder> getMyOrders(Long userId, int pageNum, int pageSize) {
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        Page<StorageOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<StorageOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StorageOrder::getUserId, userId)
                .orderByDesc(StorageOrder::getCreatedAt);

        return this.page(page, wrapper);
    }

    /**
     * 查询订单详情
     *
     * @param orderId 订单ID
     * @param userId 当前用户ID（用于权限校验）
     * @return 订单详情
     */
    public StorageOrder getOrderDetail(Long orderId, Long userId) {
        if (orderId == null) {
            throw new BusinessException("订单ID不能为空");
        }
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        StorageOrder order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(BusinessException.ORDER_NOT_FOUND, "订单不存在");
        }

        // 校验订单归属
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(BusinessException.ORDER_NOT_OWNER, "无权查看该订单");
        }

        return order;
    }

    /**
     * 验证取件码并计算费用
     *
     * 业务流程：
     * 1. 校验订单归属
     * 2. 校验订单状态（必须是 STORED）
     * 3. 验证取件码是否匹配
     * 4. 计算费用
     * 5. 将订单状态更新为 PENDING_PAYMENT
     * 6. 返回费用信息
     *
     * @param request 取件验证请求
     * @return 取件验证响应（包含费用信息）
     */
    @Transactional(rollbackFor = Exception.class)
    public PickupVerifyResponse verifyPickup(PickupVerifyRequest request) {
        log.info("开始验证取件码, orderId={}, userId={}", request.getOrderId(), request.getUserId());

        // 1. 校验参数
        if (request.getOrderId() == null) {
            throw new BusinessException("订单ID不能为空");
        }
        if (request.getUserId() == null) {
            throw new BusinessException("用户未登录");
        }
        if (request.getPickupCode() == null || request.getPickupCode().isEmpty()) {
            throw new BusinessException("请输入取件码");
        }

        // 2. 查询订单
        StorageOrder order = this.getById(request.getOrderId());
        if (order == null) {
            throw new BusinessException(BusinessException.ORDER_NOT_FOUND, "订单不存在");
        }

        // 3. 校验订单归属
        if (!order.getUserId().equals(request.getUserId())) {
            throw new BusinessException(BusinessException.ORDER_NOT_OWNER, "无权操作该订单");
        }

        // 4. 校验订单状态
        if (StorageOrder.OrderStatus.COMPLETED.equals(order.getStatus())) {
            throw new BusinessException(BusinessException.ORDER_ALREADY_COMPLETED, "该订单已完成，不能重复取件");
        }
        if (!StorageOrder.OrderStatus.STORED.equals(order.getStatus())) {
            throw new BusinessException(BusinessException.ORDER_STATUS_ERROR,
                    "订单状态为[" + order.getStatus() + "]，不能取件");
        }

        // 5. 验证取件码
        boolean codeMatched = pickupCodeGenerator.verifyCode(
                request.getPickupCode(),
                order.getPickupCodeHash()
        );

        if (!codeMatched) {
            throw new BusinessException(BusinessException.PICKUP_CODE_ERROR, "取件码错误，请重新输入");
        }

        log.info("取件码验证通过, orderId={}", request.getOrderId());

        // 6. ✅ 获取柜格信息
        LockerCell cell = lockerCellService.getCellById(order.getCellId());
        if (cell == null) {
            throw new BusinessException("柜格不存在");
        }

        // 7. ✅ 获取柜格规格（枚举转字符串）
        String sizeType = cell.getSizeType().name();  // SMALL / MEDIUM / LARGE

        // 8. 计算费用
        LocalDateTime now = LocalDateTime.now();
        BigDecimal amount = priceRuleService.calculateFee(sizeType, order.getStartTime(), now);

        if (amount == null) {
            throw new BusinessException("计费规则不存在，请联系管理员");
        }

        // 获取计费规则详情
        PriceRule rule = priceRuleService.getEnabledRuleBySizeType(sizeType);

        // 计算详细数据
        long actualMinutes = ChronoUnit.MINUTES.between(order.getStartTime(), now);
        long chargeableMinutes = Math.max(0, actualMinutes - rule.getFreeMinutes());
        long units = (chargeableMinutes + rule.getUnitMinutes() - 1) / rule.getUnitMinutes();

        // 9. 更新订单状态
        order.setStatus(StorageOrder.OrderStatus.PENDING_PAYMENT);
        order.setAmount(amount);
        this.updateById(order);

        // 10. 获取柜格编号
        String cellNo = cell.getCellNo();

        return PickupVerifyResponse.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .cellNo(cellNo)
                .startTime(order.getStartTime())
                .currentTime(now)
                .actualMinutes(actualMinutes)
                .freeMinutes(rule.getFreeMinutes())
                .chargeableMinutes(chargeableMinutes)
                .unitMinutes(rule.getUnitMinutes())
                .unitPrice(rule.getUnitPrice())
                .capAmount(rule.getCapAmount())
                .amount(amount)
                .message("取件码验证成功！请确认支付 " + amount + " 元")
                .build();
    }

    /**
     * 完成取件（模拟支付并释放柜格）
     *
     * 业务流程：
     * 1. 校验订单状态（必须是 PENDING_PAYMENT）
     * 2. 模拟支付（直接成功）
     * 3. 释放柜格
     * 4. 更新订单状态为 COMPLETED
     *
     * @param orderId 订单ID
     * @param userId 当前用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(Long orderId, Long userId) {
        log.info("开始完成取件, orderId={}, userId={}", orderId, userId);

        // 1. 校验参数
        if (orderId == null) {
            throw new BusinessException("订单ID不能为空");
        }
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        // 2. 查询订单
        StorageOrder order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(BusinessException.ORDER_NOT_FOUND, "订单不存在");
        }

        // 3. 校验订单归属
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(BusinessException.ORDER_NOT_OWNER, "无权操作该订单");
        }

        // 4. 校验订单状态
        if (StorageOrder.OrderStatus.COMPLETED.equals(order.getStatus())) {
            throw new BusinessException(BusinessException.ORDER_ALREADY_COMPLETED, "该订单已完成");
        }
        if (!StorageOrder.OrderStatus.PENDING_PAYMENT.equals(order.getStatus())) {
            throw new BusinessException(BusinessException.ORDER_STATUS_ERROR,
                    "订单状态为[" + order.getStatus() + "]，不能完成支付");
        }

        // 5. 模拟支付成功
        log.info("模拟支付成功, orderId={}, amount={}", orderId, order.getAmount());

        // 6. ✅ 释放柜格（状态改为 AVAILABLE）
        boolean released = lockerCellService.changeCellStatus(order.getCellId(), CellStatus.AVAILABLE);
        if (!released) {
            throw new BusinessException("释放柜格失败，请联系管理员");
        }
        log.info("柜格释放成功, cellId={}", order.getCellId());

        // 7. 更新订单状态
        order.setStatus(StorageOrder.OrderStatus.COMPLETED);
        order.setPaymentStatus(StorageOrder.PaymentStatus.PAID);
        order.setEndTime(LocalDateTime.now());

        boolean updated = this.updateById(order);
        if (!updated) {
            throw new BusinessException("更新订单状态失败，请重试");
        }

        log.info("订单完成, orderId={}", orderId);
    }

    // ========== 管理员方法 ==========

    /**
     * 管理员分页查询所有订单
     *
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @param status 订单状态（可选，筛选）
     * @return 订单分页数据
     */
    public Page<StorageOrder> adminGetOrders(int pageNum, int pageSize, String status) {
        Page<StorageOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<StorageOrder> wrapper = new LambdaQueryWrapper<>();

        if (status != null && !status.isEmpty()) {
            wrapper.eq(StorageOrder::getStatus, status);
        }

        wrapper.orderByDesc(StorageOrder::getCreatedAt);
        return this.page(page, wrapper);
    }

    /**
     * 管理员处理异常订单
     *
     * 将异常订单调整为指定状态
     *
     * @param orderId 订单ID
     * @param targetStatus 目标状态（STORED、COMPLETED、CANCELLED）
     * @param adminId 管理员ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void fixExceptionOrder(Long orderId, String targetStatus, Long adminId) {
        log.info("管理员处理异常订单, orderId={}, targetStatus={}, adminId={}",
                orderId, targetStatus, adminId);

        // 1. 校验参数
        if (orderId == null) {
            throw new BusinessException("订单ID不能为空");
        }
        if (targetStatus == null || targetStatus.isEmpty()) {
            throw new BusinessException("目标状态不能为空");
        }
        if (adminId == null) {
            throw new BusinessException("管理员ID不能为空");
        }

        // 2. 校验目标状态是否合法
        List<String> allowedStatuses = List.of(
                StorageOrder.OrderStatus.STORED,
                StorageOrder.OrderStatus.COMPLETED,
                StorageOrder.OrderStatus.CANCELLED
        );
        if (!allowedStatuses.contains(targetStatus)) {
            throw new BusinessException("目标状态不合法，只能调整为 STORED、COMPLETED 或 CANCELLED");
        }

        // 3. 查询订单
        StorageOrder order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(BusinessException.ORDER_NOT_FOUND, "订单不存在");
        }

        // 4. 校验当前状态是否为 EXCEPTION
        if (!StorageOrder.OrderStatus.EXCEPTION.equals(order.getStatus())) {
            throw new BusinessException(BusinessException.ORDER_STATUS_ERROR,
                    "订单状态为[" + order.getStatus() + "]，不是异常订单");
        }

        // 5. 更新状态
        order.setStatus(targetStatus);
        boolean updated = this.updateById(order);

        if (!updated) {
            throw new BusinessException("更新订单状态失败，请重试");
        }

        // 6. 如果调整为 COMPLETED，需要释放柜格
        if (StorageOrder.OrderStatus.COMPLETED.equals(targetStatus)) {
            // 释放柜格
            lockerCellService.changeCellStatus(order.getCellId(), CellStatus.AVAILABLE);
            log.info("释放柜格, cellId={}", order.getCellId());
        }
        // 更新状态
        order.setStatus(targetStatus);
        this.updateById(order);
        log.info("异常订单处理完成, orderId={}, newStatus={}", orderId, targetStatus);
    }

    /**
     * 获取简单统计数据
     *
     * @return 统计数据
     */
    public java.util.Map<String, Object> getStatistics() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();

        // 总订单数
        long totalOrders = this.count();
        stats.put("totalOrders", totalOrders);

        // 各状态订单数
        for (String status : new String[]{
                StorageOrder.OrderStatus.STORED,
                StorageOrder.OrderStatus.PENDING_PAYMENT,
                StorageOrder.OrderStatus.COMPLETED,
                StorageOrder.OrderStatus.CANCELLED,
                StorageOrder.OrderStatus.EXCEPTION
        }) {
            LambdaQueryWrapper<StorageOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(StorageOrder::getStatus, status);
            stats.put("status_" + status, this.count(wrapper));
        }

        // 累计收入（已完成订单的金额总和）
        LambdaQueryWrapper<StorageOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StorageOrder::getStatus, StorageOrder.OrderStatus.COMPLETED);
        List<StorageOrder> completedOrders = this.list(wrapper);
        BigDecimal totalRevenue = completedOrders.stream()
                .map(StorageOrder::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalRevenue", totalRevenue);

        return stats;
    }
}