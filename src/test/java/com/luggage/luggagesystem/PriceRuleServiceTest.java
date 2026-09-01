package com.luggage.luggagesystem;

import com.luggage.luggagesystem.entity.PriceRule;
import com.luggage.luggagesystem.service.PriceRuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional  // 测试结束后自动回滚，不污染数据库
class PriceRuleServiceIntegrationTest {

    @Autowired
    private PriceRuleService priceRuleService;

    /**
     * 每个测试方法执行前，先插入测试数据
     */
    @BeforeEach
    void setUp() {
        System.out.println("📦 准备测试数据...");

        // 先删除旧数据（可选）
        // priceRuleService.remove(new LambdaQueryWrapper<>());

        // 插入小柜规则
        PriceRule small = new PriceRule();
        small.setSizeType("SMALL");
        small.setUnitMinutes(60);
        small.setUnitPrice(BigDecimal.valueOf(2));
        small.setFreeMinutes(30);
        small.setCapAmount(BigDecimal.valueOf(20));
        small.setEnabled(1);
        priceRuleService.save(small);

        // 插入中柜规则
        PriceRule medium = new PriceRule();
        medium.setSizeType("MEDIUM");
        medium.setUnitMinutes(60);
        medium.setUnitPrice(BigDecimal.valueOf(4));
        medium.setFreeMinutes(30);
        medium.setCapAmount(BigDecimal.valueOf(35));
        medium.setEnabled(1);
        priceRuleService.save(medium);

        // 插入大柜规则
        PriceRule large = new PriceRule();
        large.setSizeType("LARGE");
        large.setUnitMinutes(60);
        large.setUnitPrice(BigDecimal.valueOf(6));
        large.setFreeMinutes(30);
        large.setCapAmount(BigDecimal.valueOf(50));
        large.setEnabled(1);
        priceRuleService.save(large);

        System.out.println("✅ 测试数据准备完成！");
        System.out.println();
    }

    /**
     * 测试：真实调用 PriceRuleService 计算费用
     */
    @Test
    void testCalculateFeeWithRealDatabase() {
        System.out.println("========== 测试：从数据库读取规则计算费用 ==========");

        // 测试小柜：寄存31分钟 → 应该收费2元
        LocalDateTime start = LocalDateTime.now().minusMinutes(31);
        LocalDateTime end = LocalDateTime.now();

        BigDecimal fee = priceRuleService.calculateFee("SMALL", start, end);

        assertNotNull(fee);
        // ✅ 使用 compareTo 比较，0 表示相等
        assertEquals(0, fee.compareTo(BigDecimal.valueOf(2)));
        System.out.println("✅ 小柜寄存31分钟，费用：" + fee + "元");

        // 测试中柜：寄存61分钟 → 4元
        start = LocalDateTime.now().minusMinutes(61);
        end = LocalDateTime.now();

        fee = priceRuleService.calculateFee("MEDIUM", start, end);

        assertNotNull(fee);
        assertEquals(0, fee.compareTo(BigDecimal.valueOf(4)));
        System.out.println("✅ 中柜寄存61分钟，费用：" + fee + "元");

        // 测试大柜：寄存90分钟 → 6元
        start = LocalDateTime.now().minusMinutes(90);
        end = LocalDateTime.now();

        fee = priceRuleService.calculateFee("LARGE", start, end);

        assertNotNull(fee);
        assertEquals(0, fee.compareTo(BigDecimal.valueOf(6)));
        System.out.println("✅ 大柜寄存90分钟，费用：" + fee + "元");
    }

    /**
     * 测试：查询所有启用的规则
     */
    @Test
    void testGetAllEnabledRules() {
        System.out.println("========== 测试：查询所有启用的规则 ==========");

        var rules = priceRuleService.getAllEnabledRules();

        assertEquals(3, rules.size());
        for (PriceRule rule : rules) {
            System.out.println("  规格: " + rule.getSizeType() +
                    ", 单价: " + rule.getUnitPrice() + "元" +
                    ", 免费: " + rule.getFreeMinutes() + "分钟");
        }
        System.out.println("✅ 共 " + rules.size() + " 条启用规则");
    }

    /**
     * 测试：计费阶梯
     */
    @Test
    void testFeeLadder() {
        System.out.println();
        System.out.println("========== 计费阶梯测试（小柜） ==========");

        System.out.println("寄存时长\t费用");
        System.out.println("------------------------");

        for (int minutes = 0; minutes <= 600; minutes += 30) {
            LocalDateTime end = LocalDateTime.now();
            LocalDateTime start = end.minusMinutes(minutes);

            BigDecimal fee = priceRuleService.calculateFee("SMALL", start, end);
            System.out.printf("%d分钟\t\t%.2f元%n", minutes, fee);
        }
    }
}