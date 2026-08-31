package com.luggage.luggagesystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.luggage.luggagesystem.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 操作日志Mapper
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {

    /**
     * 查询某个管理员的最新操作日志
     */
    @Select("SELECT * FROM operation_log WHERE operator_id = #{operatorId} ORDER BY created_at DESC LIMIT #{limit}")
    List<OperationLog> selectLatestByOperatorId(@Param("operatorId") Long operatorId, @Param("limit") int limit);
}