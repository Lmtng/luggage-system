package com.luggage.luggagesystem;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luggage.luggagesystem.dto.CreateOrderRequest;
import com.luggage.luggagesystem.dto.CreateOrderResponse;
import com.luggage.luggagesystem.dto.PickupVerifyRequest;
import com.luggage.luggagesystem.dto.PickupVerifyResponse;
import com.luggage.luggagesystem.entity.PriceRule;
import com.luggage.luggagesystem.entity.StorageOrder;
import com.luggage.luggagesystem.exception.BusinessException;
import com.luggage.luggagesystem.service.OrderNoGenerator;
import com.luggage.luggagesystem.service.PickupCodeGenerator;
import com.luggage.luggagesystem.service.PriceRuleService;
import com.luggage.luggagesystem.service.StorageOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional          // 每个测试在事务中运行
@Rollback               // 测试结束后自动回滚
class StorageOrderServiceTest {

    @Autowired
    private StorageOrderService storageOrderService;

    @Autowired
    private PriceRuleService priceRuleService;

    @Autowired
    private PickupCodeGenerator pickupCodeGenerator;

    private static final Long TEST_USER_ID = 100L;
    private static final Long TEST_ADMIN_ID = 1L;
    private static final Long TEST_CELL_ID = 1L;
    private static final String TEST_SIZE_TYPE = "SMALL";

    @BeforeEach
    void setUp() {
        System.out.println("📦 准备测试环境...");

        // 1. 清理所有测试用户的订单（不止一个用户）
        LambdaQueryWrapper<StorageOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(StorageOrder::getUserId, TEST_USER_ID, TEST_USER_ID + 1, TEST_USER_ID + 2,
                TEST_USER_ID + 3, TEST_USER_ID + 4, TEST_USER_ID + 5);
        storageOrderService.remove(wrapper);

        // 2. 确保计费规则存在
        PriceRule rule = priceRuleService.getEnabledRuleBySizeType(TEST_SIZE_TYPE);
        if (rule == null) {
            System.out.println("插入测试计费规则...");
            PriceRule newRule = new PriceRule();
            newRule.setSizeType(TEST_SIZE_TYPE);
            newRule.setUnitMinutes(60);
            newRule.setUnitPrice(BigDecimal.valueOf(2));
            newRule.setFreeMinutes(30);
            newRule.setCapAmount(BigDecimal.valueOf(20));
            newRule.setEnabled(1);
            priceRuleService.save(newRule);
            System.out.println("✅ 测试计费规则插入成功");
        } else {
            System.out.println("✅ 计费规则已存在");
        }

        // 3. 重置错误计数（通过反射或直接调用）
        // 由于 errorCountMap 是 private static，测试环境可以忽略
        System.out.println("✅ 测试环境准备完成");
        System.out.println();
    }

    // ==================== 创建订单测试 ====================

    @Test
    void testCreateOrder_Success() {
        System.out.println("========== 测试1：创建订单（正常流程） ==========");

        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(TEST_USER_ID);
        request.setCellId(TEST_CELL_ID);

        CreateOrderResponse response = storageOrderService.createOrder(request);

        assertNotNull(response);
        assertNotNull(response.getOrderId());
        assertNotNull(response.getOrderNo());
        assertNotNull(response.getPickupCode());
        assertEquals(6, response.getPickupCode().length());

        System.out.println("✅ 订单创建成功");
        System.out.println("   - 订单号: " + response.getOrderNo());
        System.out.println("   - 取件码: " + response.getPickupCode());
        System.out.println();
    }

    @Test
    void testCreateOrder_NoUserId() {
        System.out.println("========== 测试2：创建订单（未登录） ==========");

        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(null);
        request.setCellId(TEST_CELL_ID);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> storageOrderService.createOrder(request)
        );

        assertEquals("用户未登录", exception.getMessage());
        System.out.println("✅ 正确抛出异常: " + exception.getMessage());
        System.out.println();
    }

    @Test
    void testCreateOrder_NoCellId() {
        System.out.println("========== 测试3：创建订单（未选择柜格） ==========");

        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(TEST_USER_ID);
        request.setCellId(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> storageOrderService.createOrder(request)
        );

        assertEquals("请选择柜格", exception.getMessage());
        System.out.println("✅ 正确抛出异常: " + exception.getMessage());
        System.out.println();
    }

    // ==================== 查询订单测试 ====================

    @Test
    void testGetMyOrders() {
        System.out.println("========== 测试4：查询个人订单列表 ==========");

        // 创建3个订单
        for (int i = 1; i <= 3; i++) {
            CreateOrderRequest request = new CreateOrderRequest();
            request.setUserId(TEST_USER_ID);
            request.setCellId(TEST_CELL_ID + i - 1);
            storageOrderService.createOrder(request);
        }

        var page = storageOrderService.getMyOrders(TEST_USER_ID, 1, 10);

        assertNotNull(page);
        assertEquals(3, page.getTotal());

        System.out.println("✅ 查询个人订单列表成功");
        System.out.println("   - 总订单数: " + page.getTotal());
        System.out.println();
    }

    @Test
    void testGetOrderDetail() {
        System.out.println("========== 测试5：查询订单详情 ==========");

        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(TEST_USER_ID);
        request.setCellId(TEST_CELL_ID);
        CreateOrderResponse createResponse = storageOrderService.createOrder(request);

        StorageOrder order = storageOrderService.getOrderDetail(
                createResponse.getOrderId(),
                TEST_USER_ID
        );

        assertNotNull(order);
        assertEquals(createResponse.getOrderId(), order.getId());

        System.out.println("✅ 查询订单详情成功");
        System.out.println("   - 订单号: " + order.getOrderNo());
        System.out.println();
    }

    @Test
    void testGetOrderDetail_NotOwner() {
        System.out.println("========== 测试6：查询订单详情（越权访问） ==========");

        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(TEST_USER_ID);
        request.setCellId(TEST_CELL_ID);
        CreateOrderResponse createResponse = storageOrderService.createOrder(request);

        Long otherUserId = 999L;

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> storageOrderService.getOrderDetail(
                        createResponse.getOrderId(),
                        otherUserId
                )
        );

        assertEquals("无权查看该订单", exception.getMessage());
        System.out.println("✅ 正确抛出异常: " + exception.getMessage());
        System.out.println();
    }

    // ==================== 取件验证测试 ====================

    @Test
    void testVerifyPickup_Success() {
        System.out.println("========== 测试7：验证取件码（正常流程） ==========");

        // 1. 创建订单
        CreateOrderRequest createRequest = new CreateOrderRequest();
        createRequest.setUserId(TEST_USER_ID);
        createRequest.setCellId(TEST_CELL_ID);
        CreateOrderResponse createResponse = storageOrderService.createOrder(createRequest);
        String pickupCode = createResponse.getPickupCode();

        // 2. 等待2秒模拟寄存
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 3. 验证取件码
        PickupVerifyRequest verifyRequest = new PickupVerifyRequest();
        verifyRequest.setOrderId(createResponse.getOrderId());
        verifyRequest.setUserId(TEST_USER_ID);
        verifyRequest.setPickupCode(pickupCode);

        PickupVerifyResponse verifyResponse = storageOrderService.verifyPickup(verifyRequest);

        assertNotNull(verifyResponse);
        assertNotNull(verifyResponse.getAmount());
        assertTrue(verifyResponse.getAmount().compareTo(BigDecimal.ZERO) >= 0);

        System.out.println("✅ 取件码验证成功");
        System.out.println("   - 费用: " + verifyResponse.getAmount() + " 元");
        System.out.println();
    }

    @Test
    void testVerifyPickup_WrongCode() {
        System.out.println("========== 测试8：验证取件码（错误取件码） ==========");

        // 1. 创建订单
        CreateOrderRequest createRequest = new CreateOrderRequest();
        createRequest.setUserId(TEST_USER_ID);
        createRequest.setCellId(TEST_CELL_ID);
        CreateOrderResponse createResponse = storageOrderService.createOrder(createRequest);

        // 2. 使用错误的取件码
        PickupVerifyRequest verifyRequest = new PickupVerifyRequest();
        verifyRequest.setOrderId(createResponse.getOrderId());
        verifyRequest.setUserId(TEST_USER_ID);
        verifyRequest.setPickupCode("999999");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> storageOrderService.verifyPickup(verifyRequest)
        );

        assertTrue(exception.getMessage().contains("取件码错误"));
        System.out.println("✅ 正确抛出异常: " + exception.getMessage());
        System.out.println();
    }

    @Test
    void testVerifyPickup_AlreadyCompleted() {
        System.out.println("========== 测试9：验证取件码（订单已完成） ==========");

        // 1. 创建订单
        CreateOrderRequest createRequest = new CreateOrderRequest();
        createRequest.setUserId(TEST_USER_ID);
        createRequest.setCellId(TEST_CELL_ID);
        CreateOrderResponse createResponse = storageOrderService.createOrder(createRequest);
        String pickupCode = createResponse.getPickupCode();

        // 2. 验证取件码
        PickupVerifyRequest verifyRequest = new PickupVerifyRequest();
        verifyRequest.setOrderId(createResponse.getOrderId());
        verifyRequest.setUserId(TEST_USER_ID);
        verifyRequest.setPickupCode(pickupCode);
        storageOrderService.verifyPickup(verifyRequest);

        // 3. 完成取件
        storageOrderService.completeOrder(createResponse.getOrderId(), TEST_USER_ID);

        // 4. 再次验证（应该失败）
        PickupVerifyRequest verifyRequest2 = new PickupVerifyRequest();
        verifyRequest2.setOrderId(createResponse.getOrderId());
        verifyRequest2.setUserId(TEST_USER_ID);
        verifyRequest2.setPickupCode(pickupCode);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> storageOrderService.verifyPickup(verifyRequest2)
        );

        assertEquals("该订单已完成，不能重复取件", exception.getMessage());
        System.out.println("✅ 正确抛出异常: " + exception.getMessage());
        System.out.println();
    }

    @Test
    void testVerifyPickup_WrongStatus() {
        System.out.println("========== 测试10：验证取件码（订单状态错误） ==========");

        // 1. 创建订单
        CreateOrderRequest createRequest = new CreateOrderRequest();
        createRequest.setUserId(TEST_USER_ID);
        createRequest.setCellId(TEST_CELL_ID);
        CreateOrderResponse createResponse = storageOrderService.createOrder(createRequest);
        String pickupCode = createResponse.getPickupCode();

        // 2. 验证取件码（第一次，状态变为 PENDING_PAYMENT）
        PickupVerifyRequest verifyRequest = new PickupVerifyRequest();
        verifyRequest.setOrderId(createResponse.getOrderId());
        verifyRequest.setUserId(TEST_USER_ID);
        verifyRequest.setPickupCode(pickupCode);
        storageOrderService.verifyPickup(verifyRequest);

        // 3. 再次验证（应该失败，状态不是 STORED）
        PickupVerifyRequest verifyRequest2 = new PickupVerifyRequest();
        verifyRequest2.setOrderId(createResponse.getOrderId());
        verifyRequest2.setUserId(TEST_USER_ID);
        verifyRequest2.setPickupCode(pickupCode);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> storageOrderService.verifyPickup(verifyRequest2)
        );

        assertTrue(exception.getMessage().contains("不能取件") ||
                exception.getMessage().contains("订单状态"));
        System.out.println("✅ 正确抛出异常: " + exception.getMessage());
        System.out.println();
    }

    // ==================== 完成取件测试 ====================

    @Test
    void testCompleteOrder_Success() {
        System.out.println("========== 测试11：完成取件（正常流程） ==========");

        // 1. 创建订单
        CreateOrderRequest createRequest = new CreateOrderRequest();
        createRequest.setUserId(TEST_USER_ID);
        createRequest.setCellId(TEST_CELL_ID);
        CreateOrderResponse createResponse = storageOrderService.createOrder(createRequest);
        String pickupCode = createResponse.getPickupCode();

        // 2. 验证取件码
        PickupVerifyRequest verifyRequest = new PickupVerifyRequest();
        verifyRequest.setOrderId(createResponse.getOrderId());
        verifyRequest.setUserId(TEST_USER_ID);
        verifyRequest.setPickupCode(pickupCode);
        PickupVerifyResponse verifyResponse = storageOrderService.verifyPickup(verifyRequest);

        System.out.println("取件码验证成功，费用: " + verifyResponse.getAmount() + " 元");

        // 3. 完成取件
        storageOrderService.completeOrder(createResponse.getOrderId(), TEST_USER_ID);

        // 4. 验证状态
        StorageOrder completedOrder = storageOrderService.getById(createResponse.getOrderId());
        assertEquals(StorageOrder.OrderStatus.COMPLETED, completedOrder.getStatus());

        System.out.println("✅ 完成取件成功");
        System.out.println("   - 状态: " + completedOrder.getStatus());
        System.out.println();
    }

    // ==================== 管理员测试 ====================

    @Test
    void testAdminGetOrders() {
        System.out.println("========== 测试12：管理员查询所有订单 ==========");

        // 创建3个订单
        for (int i = 1; i <= 3; i++) {
            CreateOrderRequest request = new CreateOrderRequest();
            request.setUserId(TEST_USER_ID + i);
            request.setCellId(TEST_CELL_ID + i);
            storageOrderService.createOrder(request);
        }

        var page = storageOrderService.adminGetOrders(1, 10, null);

        assertNotNull(page);
        assertTrue(page.getTotal() >= 3);

        System.out.println("✅ 管理员查询所有订单成功");
        System.out.println("   - 总订单数: " + page.getTotal());
        System.out.println();
    }

    @Test
    void testAdminGetOrdersByStatus() {
        System.out.println("========== 测试13：管理员按状态筛选订单 ==========");

        // 创建1个 STORED 状态的订单
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(TEST_USER_ID);
        request.setCellId(TEST_CELL_ID);
        storageOrderService.createOrder(request);

        var page = storageOrderService.adminGetOrders(1, 10, StorageOrder.OrderStatus.STORED);

        assertNotNull(page);
        assertTrue(page.getTotal() >= 1);

        System.out.println("✅ 按状态筛选订单成功");
        System.out.println("   - STORED 订单数: " + page.getTotal());
        System.out.println();
    }

    @Test
    void testFixExceptionOrder() {
        System.out.println("========== 测试14：管理员处理异常订单 ==========");

        // 1. 创建订单
        CreateOrderRequest createRequest = new CreateOrderRequest();
        createRequest.setUserId(TEST_USER_ID);
        createRequest.setCellId(TEST_CELL_ID);
        CreateOrderResponse createResponse = storageOrderService.createOrder(createRequest);

        // 2. 手动改为 EXCEPTION
        StorageOrder order = storageOrderService.getById(createResponse.getOrderId());
        order.setStatus(StorageOrder.OrderStatus.EXCEPTION);
        storageOrderService.updateById(order);
        System.out.println("订单状态已改为: EXCEPTION");

        // 3. 管理员处理
        storageOrderService.fixExceptionOrder(
                createResponse.getOrderId(),
                StorageOrder.OrderStatus.COMPLETED,
                TEST_ADMIN_ID
        );

        StorageOrder fixedOrder = storageOrderService.getById(createResponse.getOrderId());
        assertEquals(StorageOrder.OrderStatus.COMPLETED, fixedOrder.getStatus());

        System.out.println("✅ 管理员处理异常订单成功");
        System.out.println("   - 新状态: " + fixedOrder.getStatus());
        System.out.println();
    }

    @Test
    void testFixExceptionOrder_InvalidTarget() {
        System.out.println("========== 测试15：管理员处理异常订单（目标状态不合法） ==========");

        // 1. 创建订单
        CreateOrderRequest createRequest = new CreateOrderRequest();
        createRequest.setUserId(TEST_USER_ID);
        createRequest.setCellId(TEST_CELL_ID);
        CreateOrderResponse createResponse = storageOrderService.createOrder(createRequest);

        // 2. 手动改为 EXCEPTION
        StorageOrder order = storageOrderService.getById(createResponse.getOrderId());
        order.setStatus(StorageOrder.OrderStatus.EXCEPTION);
        storageOrderService.updateById(order);

        // 3. 尝试调整为不合法状态
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> storageOrderService.fixExceptionOrder(
                        createResponse.getOrderId(),
                        StorageOrder.OrderStatus.PENDING_PAYMENT,
                        TEST_ADMIN_ID
                )
        );

        assertTrue(exception.getMessage().contains("目标状态不合法"));
        System.out.println("✅ 正确抛出异常: " + exception.getMessage());
        System.out.println();
    }

    // ==================== 统计功能测试 ====================

    @Test
    void testGetStatistics() {
        System.out.println("========== 测试16：统计功能 ==========");

        // 创建5个订单
        for (int i = 1; i <= 5; i++) {
            CreateOrderRequest request = new CreateOrderRequest();
            request.setUserId(TEST_USER_ID + i);
            request.setCellId(TEST_CELL_ID + i);
            storageOrderService.createOrder(request);
        }

        var stats = storageOrderService.getStatistics();

        assertNotNull(stats);
        assertTrue(stats.containsKey("totalOrders"));
        assertTrue((Long) stats.get("totalOrders") >= 5);

        System.out.println("✅ 统计功能测试成功");
        System.out.println("   - 总订单数: " + stats.get("totalOrders"));
        System.out.println("   - 总收入: " + stats.get("totalRevenue") + " 元");
        System.out.println();
    }

    // ==================== 工具类测试 ====================

    @Test
    void testPickupCodeGenerator() {
        System.out.println("========== 测试17：取件码生成器功能 ==========");

        String code1 = pickupCodeGenerator.generateCode();
        String code2 = pickupCodeGenerator.generateCode();

        assertNotNull(code1);
        assertNotNull(code2);
        assertEquals(6, code1.length());
        assertEquals(6, code2.length());

        String encrypted = pickupCodeGenerator.encryptCode(code1);
        assertNotNull(encrypted);
        assertNotEquals(code1, encrypted);

        boolean match = pickupCodeGenerator.verifyCode(code1, encrypted);
        assertTrue(match);

        boolean notMatch = pickupCodeGenerator.verifyCode("999999", encrypted);
        assertFalse(notMatch);

        System.out.println("✅ 取件码生成器功能测试通过");
        System.out.println();
    }

    @Test
    void testOrderNoGenerator() {
        System.out.println("========== 测试18：订单号生成器功能 ==========");

        OrderNoGenerator generator = new OrderNoGenerator();

        String orderNo1 = generator.generateOrderNo();
        String orderNo2 = generator.generateOrderNo();
        String orderNo3 = generator.generateOrderNo();

        assertNotNull(orderNo1);
        assertTrue(orderNo1.startsWith("ST"));
        assertEquals(14, orderNo1.length());

        System.out.println("✅ 订单号生成器功能测试通过");
        System.out.println("   - " + orderNo1);
        System.out.println("   - " + orderNo2);
        System.out.println("   - " + orderNo3);
        System.out.println();
    }
}