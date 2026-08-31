package com.luggage.luggagesystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.luggage.luggagesystem.entity.PriceRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 计费规则Mapper
 */
@Mapper
public interface PriceRuleMapper extends BaseMapper<PriceRule> {

    /**
     * 根据规格查询启用的计费规则
     */
    @Select("SELECT * FROM price_rule WHERE size_type = #{sizeType} AND enabled = 1")
    PriceRule selectEnabledBySizeType(@Param("sizeType") String sizeType);

    /**
     * 检查某规格是否已有启用规则
     */
    @Select("SELECT COUNT(*) FROM price_rule WHERE size_type = #{sizeType} AND enabled = 1")
    int countEnabledBySizeType(@Param("sizeType") String sizeType);
}