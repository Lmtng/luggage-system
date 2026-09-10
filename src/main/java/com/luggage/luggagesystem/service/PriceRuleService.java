package com.luggage.luggagesystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luggage.luggagesystem.entity.LockerCell;
import com.luggage.luggagesystem.entity.PriceRule;
import com.luggage.luggagesystem.enums.CellSizeType;
import com.luggage.luggagesystem.enums.CellStatus;
import com.luggage.luggagesystem.exception.BusinessException;
import com.luggage.luggagesystem.mapper.LockerCellMapper;
import com.luggage.luggagesystem.mapper.PriceRuleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Service
public class PriceRuleService extends ServiceImpl<PriceRuleMapper, PriceRule> {

    private final LockerCellMapper lockerCellMapper;

    public PriceRuleService(LockerCellMapper lockerCellMapper) {
        this.lockerCellMapper = lockerCellMapper;
    }

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
        List<PriceRule> rules = this.list();
        rules.forEach(rule -> rule.setOccupied(
                hasOccupiedCells(rule.getSizeType())
        ));
        return rules;
    }

    /**
     * 判断指定规格是否仍有用户占用柜格。
     */
    public boolean hasOccupiedCells(String sizeType) {
        CellSizeType cellSizeType = parseSizeType(sizeType);
        return lockerCellMapper.countOccupiedBySize(cellSizeType) > 0;
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
        if (rule == null || rule.getSizeType() == null) {
            throw new BusinessException("计费规则规格不能为空");
        }

        ensureNoOccupiedCells(rule.getSizeType());

        if (Objects.equals(rule.getEnabled(), PriceRule.EnabledStatus.ENABLED)) {
            Long count = this.baseMapper.selectCount(
                    new LambdaQueryWrapper<PriceRule>()
                            .eq(PriceRule::getSizeType, rule.getSizeType())
                            .eq(PriceRule::getEnabled, PriceRule.EnabledStatus.ENABLED)
            );
            if (count > 0) {
                throw new BusinessException("该规格已存在启用的计费规则，请先停用旧规则");
            }
        }
        return this.save(rule);
    }

    @Transactional
    public boolean updateRule(PriceRule rule) {
        if (rule == null || rule.getId() == null) {
            throw new BusinessException("计费规则编号不能为空");
        }

        PriceRule existRule = this.getById(rule.getId());
        if (existRule == null) {
            throw new BusinessException("计费规则不存在");
        }

        if (rule.getSizeType() == null) {
            rule.setSizeType(existRule.getSizeType());
        } else if (!existRule.getSizeType().equalsIgnoreCase(rule.getSizeType())) {
            throw new BusinessException("计费规则的柜格规格不允许修改");
        }

        ensureNoOccupiedCells(existRule.getSizeType());

        if (Objects.equals(rule.getEnabled(), PriceRule.EnabledStatus.ENABLED)) {
            PriceRule enabledRule = getEnabledRuleBySizeType(rule.getSizeType());
            if (enabledRule != null && !enabledRule.getId().equals(rule.getId())) {
                throw new BusinessException("该规格已存在其他启用的计费规则，请先停用旧规则");
            }
        }

        return this.updateById(rule);
    }

    @Transactional
    public boolean deleteRule(Long ruleId) {
        PriceRule rule = this.getById(ruleId);
        if (rule == null) {
            throw new BusinessException("计费规则不存在");
        }

        ensureNoOccupiedCells(rule.getSizeType());
        rule.setEnabled(PriceRule.EnabledStatus.DISABLED);
        return this.updateById(rule);
    }

    /**
     * 在同一事务中锁定该规格柜格并检查占用状态。
     */
    private void ensureNoOccupiedCells(String sizeType) {
        CellSizeType cellSizeType = parseSizeType(sizeType);
        List<LockerCell> cells =
                lockerCellMapper.selectBySizeForUpdate(cellSizeType);

        boolean occupied = cells.stream().anyMatch(
                cell -> CellStatus.OCCUPIED.equals(cell.getStatus())
        );

        if (occupied) {
            throw new BusinessException(
                    "该规格仍有用户正在寄存，暂不能修改计费规则"
            );
        }
    }

    private CellSizeType parseSizeType(String sizeType) {
        if (sizeType == null || sizeType.isBlank()) {
            throw new BusinessException("柜格规格不能为空");
        }

        try {
            return CellSizeType.valueOf(sizeType.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("柜格规格不合法");
        }
    }
}
