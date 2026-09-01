package com.luggage.luggagesystem;

import com.luggage.luggagesystem.entity.OperationLog;
import com.luggage.luggagesystem.service.OperationLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
// 暂时注释掉 @Transactional，让数据真正写入数据库便于调试
// @Transactional
class OperationLogServiceTest {

    @Autowired
    private OperationLogService operationLogService;

    @BeforeEach
    void setUp() {
        System.out.println("📦 准备测试...");
        long count = operationLogService.count();
        System.out.println("operation_log 表当前记录数: " + count);
        System.out.println();
    }

    @Test
    @Rollback(false)  // 不回滚，数据保留
    void testLogWithFullInfo() {
        System.out.println("========== 测试1：记录完整操作日志 ==========");

        Long operatorId = 1L;
        String operationType = OperationLog.OperationType.UPDATE_PRICE_RULE;
        String targetType = OperationLog.TargetType.PRICE_RULE;
        Long targetId = 5L;
        String detail = "修改了小柜计费规则：单价从2.00元调整为2.50元，免费时长从30分钟调整为20分钟";

        boolean result = operationLogService.log(
                operatorId,
                operationType,
                targetType,
                targetId,
                detail
        );

        assertTrue(result);
        System.out.println("✅ 完整日志记录成功");

        List<OperationLog> logs = operationLogService.getLogsByOperator(operatorId);
        assertTrue(logs.size() >= 1);
        System.out.println("✅ 验证通过");
        System.out.println();
    }

    @Test
    @Rollback(false)
    void testLogSimple() {
        System.out.println("========== 测试2：记录简化操作日志 ==========");

        Long operatorId = 2L;
        String detail = "管理员启用了A区3号柜格";

        boolean result = operationLogService.logSimple(operatorId, detail);
        assertTrue(result);
        System.out.println("✅ 简化日志记录成功");
        System.out.println();
    }

    @Test
    @Rollback(false)
    void testLogMultipleTypes() {
        System.out.println("========== 测试3：记录多种类型日志 ==========");

        Long operatorId = 3L;

        operationLogService.log(
                operatorId,
                OperationLog.OperationType.UPDATE_CELL,
                OperationLog.TargetType.CELL,
                10L,
                "将A-01柜格从AVAILABLE改为DISABLED"
        );

        operationLogService.log(
                operatorId,
                OperationLog.OperationType.UPDATE_PRICE_RULE,
                OperationLog.TargetType.PRICE_RULE,
                3L,
                "修改了大柜计费规则"
        );

        operationLogService.log(
                operatorId,
                OperationLog.OperationType.FIX_EXCEPTION_ORDER,
                OperationLog.TargetType.ORDER,
                100L,
                "处理异常订单"
        );

        List<OperationLog> logs = operationLogService.getLogsByOperator(operatorId);
        assertTrue(logs.size() >= 3);
        System.out.println("✅ 多种类型日志记录成功，共 " + logs.size() + " 条");
        System.out.println();
    }

    @Test
    @Rollback(false)
    void testGetLogsByOperator() {
        System.out.println("========== 测试4：查询管理员的所有日志 ==========");

        Long operatorId = 4L;

        for (int i = 1; i <= 5; i++) {
            operationLogService.logSimple(operatorId, "测试操作 " + i);
        }

        List<OperationLog> logs = operationLogService.getLogsByOperator(operatorId);

        assertNotNull(logs);
        assertTrue(logs.size() >= 5, "期望至少5条，实际 " + logs.size());

        System.out.println("✅ 查询成功，共 " + logs.size() + " 条日志");
        System.out.println();
    }

    @Test
    @Rollback(false)
    void testGetLogsPage() {
        System.out.println("========== 测试5：分页查询所有日志 ==========");

        Long operatorId = 5L;

        for (int i = 1; i <= 10; i++) {
            operationLogService.logSimple(operatorId, "测试操作 " + i);
        }

        var page1 = operationLogService.getLogsPage(1, 3);
        assertEquals(3, page1.getRecords().size());

        System.out.println("✅ 第1页查询成功");
        System.out.println("   - 当前页记录数: " + page1.getRecords().size());
        System.out.println("   - 总记录数: " + page1.getTotal());
        System.out.println();
    }

    @Test
    @Rollback(false)
    void testGetLogsByType() {
        System.out.println("========== 测试6：按操作类型查询日志 ==========");

        Long operatorId = 6L;

        operationLogService.log(
                operatorId,
                OperationLog.OperationType.UPDATE_CELL,
                OperationLog.TargetType.CELL,
                1L,
                "修改柜格状态"
        );
        operationLogService.log(
                operatorId,
                OperationLog.OperationType.UPDATE_CELL,
                OperationLog.TargetType.CELL,
                2L,
                "修改柜格规格"
        );
        operationLogService.log(
                operatorId,
                OperationLog.OperationType.UPDATE_PRICE_RULE,
                OperationLog.TargetType.PRICE_RULE,
                1L,
                "修改计费规则"
        );

        var cellLogs = operationLogService.getLogsByType(
                OperationLog.OperationType.UPDATE_CELL, 1, 10
        );

        assertNotNull(cellLogs);
        assertTrue(cellLogs.getTotal() >= 2);

        System.out.println("✅ 查询 UPDATE_CELL 类型日志成功，共 " + cellLogs.getTotal() + " 条");
        System.out.println();
    }

    /**
     * 测试7：查询最近的N条日志
     *
     * 修复：使用 @Rollback(false) 确保数据保留
     */
    @Test
    @Rollback(false)
    void testGetLatestLogs() {
        System.out.println("========== 测试7：查询最近的N条日志 ==========");

        Long operatorId = 7L;

        // 插入10条日志
        System.out.println("插入10条日志...");
        for (int i = 1; i <= 10; i++) {
            boolean result = operationLogService.logSimple(
                    operatorId,
                    "日志序号: " + i
            );
            if (!result) {
                System.err.println("⚠️ 第" + i + "条插入失败");
            }
        }

        // 查询所有日志，确认数据已插入
        long totalCount = operationLogService.count();
        System.out.println("operation_log 表总记录数: " + totalCount);

        // 查询该管理员的日志
        List<OperationLog> adminLogs = operationLogService.getLogsByOperator(operatorId);
        System.out.println("该管理员日志数: " + adminLogs.size());

        // 查询最近的5条（所有管理员）
        List<OperationLog> latest5 = operationLogService.getLatestLogs(5);

        // 打印结果
        System.out.println("最近的5条日志:");
        for (int i = 0; i < latest5.size(); i++) {
            OperationLog log = latest5.get(i);
            System.out.println("  " + (i + 1) + ". id=" + log.getId() +
                    ", operatorId=" + log.getOperatorId() +
                    ", detail=" + log.getDetail());
        }

        // 断言：至少有5条
        assertNotNull(latest5);
        assertTrue(latest5.size() >= 5, "期望至少5条，实际 " + latest5.size());

        System.out.println("✅ 查询最近的5条日志成功");
        System.out.println();
    }

    @Test
    @Rollback(false)
    void testGetLatestLogsByOperator() {
        System.out.println("========== 测试8：查询管理员最近的N条日志 ==========");

        Long operatorId = 8L;

        for (int i = 1; i <= 8; i++) {
            operationLogService.logSimple(operatorId, "管理员8的操作 #" + i);
        }

        operationLogService.logSimple(99L, "其他管理员的操作");

        List<OperationLog> latest3 = operationLogService.getLatestLogsByOperator(operatorId, 3);

        assertNotNull(latest3);
        assertEquals(3, latest3.size());

        for (OperationLog log : latest3) {
            assertEquals(operatorId, log.getOperatorId());
        }

        System.out.println("✅ 查询管理员" + operatorId + "最近的3条日志成功");
        System.out.println();
    }

    @Test
    @Rollback(false)
    void testRealBusinessScenario() {
        System.out.println("========== 测试9：模拟真实业务场景 ==========");

        Long adminId = 10L;

        operationLogService.logSimple(adminId, "管理员登录系统");
        operationLogService.logSimple(adminId, "查看今日订单列表");
        operationLogService.log(
                adminId,
                OperationLog.OperationType.FIX_EXCEPTION_ORDER,
                OperationLog.TargetType.ORDER,
                1001L,
                "发现订单异常，开始处理"
        );
        operationLogService.log(
                adminId,
                OperationLog.OperationType.UPDATE_PRICE_RULE,
                OperationLog.TargetType.PRICE_RULE,
                1L,
                "修改计费规则"
        );
        operationLogService.log(
                adminId,
                OperationLog.OperationType.DISABLE_CELL,
                OperationLog.TargetType.CELL,
                5L,
                "A-05柜格故障，已停用"
        );

        List<OperationLog> logs = operationLogService.getLogsByOperator(adminId);
        assertTrue(logs.size() >= 5);

        System.out.println("✅ 真实业务场景模拟完成！共 " + logs.size() + " 条日志");
        System.out.println();
    }

    @Test
    @Rollback(false)
    void testAutoFill() {
        System.out.println("========== 测试10：验证自动填充功能 ==========");

        Long operatorId = 11L;

        boolean result = operationLogService.logSimple(operatorId, "测试自动填充功能");
        assertTrue(result);

        List<OperationLog> logs = operationLogService.getLatestLogsByOperator(operatorId, 1);
        assertTrue(logs.size() >= 1);

        OperationLog savedLog = logs.get(0);
        assertNotNull(savedLog.getCreatedAt());

        System.out.println("✅ 自动填充验证通过");
        System.out.println("   - created_at: " + savedLog.getCreatedAt());
        System.out.println();
    }
}