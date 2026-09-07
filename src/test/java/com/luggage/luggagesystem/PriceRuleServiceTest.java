package com.luggage.luggagesystem;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luggage.luggagesystem.entity.PriceRule;
import com.luggage.luggagesystem.mapper.PriceRuleMapper;
import com.luggage.luggagesystem.service.PriceRuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PriceRuleServiceIntegrationTest {

    @Autowired
    private PriceRuleService priceRuleService;

    @Autowired
    private PriceRuleMapper priceRuleMapper;

    private PriceRule testRule;

    @BeforeEach
    void setUp() {

        /*
         * 建表脚本可能已经添加了SMALL默认规则。
         * 每次测试前先删除原有SMALL规则，避免唯一索引冲突。
         * 测试类有@Transactional，测试结束后会自动回滚。
         */
        priceRuleMapper.delete(
                new LambdaQueryWrapper<PriceRule>()
                        .eq(
                                PriceRule::getSizeType,
                                PriceRule.SizeType.SMALL
                        )
        );

        testRule = new PriceRule();
        testRule.setSizeType(
                PriceRule.SizeType.SMALL
        );
        testRule.setUnitMinutes(60);
        testRule.setUnitPrice(
                new BigDecimal("5.00")
        );
        testRule.setFreeMinutes(30);
        testRule.setCapAmount(
                new BigDecimal("50.00")
        );
        testRule.setEnabled(
                PriceRule.EnabledStatus.ENABLED
        );

        priceRuleMapper.insert(testRule);
    }

    @Test
    void testGetAllEnabledRules() {

        List<PriceRule> rules =
                priceRuleService.getAllEnabledRules();

        assertNotNull(rules);
        assertFalse(rules.isEmpty());

        assertTrue(
                rules.stream().allMatch(
                        rule ->
                                PriceRule.EnabledStatus.ENABLED
                                        .equals(rule.getEnabled())
                )
        );
    }

    @Test
    void testCalculateFeeWithRealDatabase() {

        LocalDateTime startTime =
                LocalDateTime.now().minusMinutes(150);

        LocalDateTime endTime =
                LocalDateTime.now();

        BigDecimal fee =
                priceRuleService.calculateFee(
                        "SMALL",
                        startTime,
                        endTime
                );

        assertNotNull(fee);
        assertEquals(
                new BigDecimal("10.00"),
                fee
        );
    }

    @Test
    void testFeeLadder() {

        LocalDateTime now =
                LocalDateTime.now();

        // 30分钟内免费
        BigDecimal fee1 =
                priceRuleService.calculateFee(
                        "SMALL",
                        now.minusMinutes(20),
                        now
                );

        assertEquals(
                new BigDecimal("0.00"),
                fee1
        );

        // 90分钟减去免费30分钟，收费5元
        BigDecimal fee2 =
                priceRuleService.calculateFee(
                        "SMALL",
                        now.minusMinutes(90),
                        now
                );

        assertEquals(
                new BigDecimal("5.00"),
                fee2
        );

        // 91分钟需要两个计费单位，收费10元
        BigDecimal fee3 =
                priceRuleService.calculateFee(
                        "SMALL",
                        now.minusMinutes(91),
                        now
                );

        assertEquals(
                new BigDecimal("10.00"),
                fee3
        );

        // 超过封顶金额时按50元计算
        BigDecimal fee4 =
                priceRuleService.calculateFee(
                        "SMALL",
                        now.minusMinutes(1000),
                        now
                );

        assertEquals(
                new BigDecimal("50.00"),
                fee4
        );
    }
}