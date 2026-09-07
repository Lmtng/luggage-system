package com.luggage.luggagesystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luggage.luggagesystem.entity.PriceRule;
import com.luggage.luggagesystem.mapper.PriceRuleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class PriceRuleService extends ServiceImpl<PriceRuleMapper, PriceRule> {

    // ========== 查询方法 ==========

    public PriceRule getEnabledRuleBySizeType(String sizeType) {
        LambdaQueryWrapper<PriceRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PriceRule::getSizeType, sizeType)
                .eq(PriceRule::getEnabled, PriceRule.EnabledStatus.ENABLED);
        return this.getOne(wrapper);
    }

    public List<PriceRule> getAllEnabledRules() {
        LambdaQueryWrapper<PriceRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PriceRule::getEnabled, PriceRule.EnabledStatus.ENABLED);
        return this.list(wrapper);
    }

    public List<PriceRule> getAllRules() {
        return this.list();
    }

    // ========== 核心计费逻辑 ==========

    public BigDecimal calculateFee(String sizeType, LocalDateTime startTime, LocalDateTime endTime) {
        PriceRule rule = getEnabledRuleBySizeType(sizeType);
        if (rule == null) {
            return null;
        }

        long actualMinutes = ChronoUnit.MINUTES.between(startTime, endTime);
        if (actualMinutes < 0) {
            actualMinutes = 0;
        }

        Integer freeMinutes = rule.getFreeMinutes();
        long freeMin = (freeMinutes != null) ? freeMinutes.longValue() : 0L;
        long chargeableMinutes = actualMinutes - freeMin;
        if (chargeableMinutes < 0) {
            chargeableMinutes = 0;
        }

        Integer unitMinutesObj = rule.getUnitMinutes();
        int unitMinutes = (unitMinutesObj != null && unitMinutesObj > 0) ? unitMinutesObj : 60;
        long units = (chargeableMinutes + unitMinutes - 1) / unitMinutes;

        BigDecimal unitPrice = rule.getUnitPrice();
        if (unitPrice == null) {
            unitPrice = BigDecimal.ZERO;
        }
        BigDecimal fee = unitPrice.multiply(BigDecimal.valueOf(units));

        if (rule.getCapAmount() != null && fee.compareTo(rule.getCapAmount()) > 0) {
            fee = rule.getCapAmount();
        }

        return fee;
    }

    public BigDecimal calculateFee(String sizeType, LocalDateTime startTime) {
        return calculateFee(sizeType, startTime, LocalDateTime.now());
    }

    // ========== 管理员管理方法 ==========

    @Transactional
    public boolean addRule(PriceRule rule) {
        if (rule.getEnabled() == PriceRule.EnabledStatus.ENABLED) {
            Long count = this.baseMapper.selectCount(
                    new LambdaQueryWrapper<PriceRule>()
                            .eq(PriceRule::getSizeType, rule.getSizeType())
                            .eq(PriceRule::getEnabled, PriceRule.EnabledStatus.ENABLED)
            );
            if (count > 0) {
                throw new RuntimeException("该规格已存在启用的计费规则，请先停用旧规则");
            }
        }
        return this.save(rule);
    }

    @Transactional
    public boolean updateRule(PriceRule rule) {
        PriceRule existRule = this.getById(rule.getId());
        if (existRule == null) {
            throw new RuntimeException("计费规则不存在");
        }

        if (rule.getEnabled() == PriceRule.EnabledStatus.ENABLED) {
            PriceRule enabledRule = getEnabledRuleBySizeType(rule.getSizeType());
            if (enabledRule != null && !enabledRule.getId().equals(rule.getId())) {
                throw new RuntimeException("该规格已存在其他启用的计费规则，请先停用旧规则");
            }
        }

        return this.updateById(rule);
    }

    @Transactional
    public boolean deleteRule(Long ruleId) {
        PriceRule rule = this.getById(ruleId);
        if (rule == null) {
            throw new RuntimeException("计费规则不存在");
        }
        rule.setEnabled(PriceRule.EnabledStatus.DISABLED);
        return this.updateById(rule);
    }
}