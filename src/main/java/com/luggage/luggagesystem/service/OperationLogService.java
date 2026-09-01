package com.luggage.luggagesystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luggage.luggagesystem.entity.OperationLog;
import com.luggage.luggagesystem.mapper.OperationLogMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 操作日志服务类
 *
 * 功能说明：
 * 1. 记录管理员的重要操作（柜格修改、规则修改、异常订单处理等）
 * 2. 查询操作日志（用于审计追踪）
 *
 * 设计思想：
 * 操作日志是"写多读少"的数据，主要用于事后审计和问题排查。
 * 因此本服务只提供写入和查询功能，不提供修改和删除。
 *
 * @author 成员B
 * @date 2026-08-31
 */
@Service
public class OperationLogService extends ServiceImpl<OperationLogMapper, OperationLog> {

    // ========== 写入方法 ==========

    /**
     * 记录操作日志
     *
     * 这是最常用的方法，管理员执行重要操作后调用此方法记录日志
     *
     * @param operatorId 操作人ID（管理员ID）
     * @param operationType 操作类型，使用 OperationLog.OperationType 常量
     * @param targetType 目标类型，使用 OperationLog.TargetType 常量
     * @param targetId 目标记录ID（可为null）
     * @param detail 操作详细说明
     * @return 是否记录成功
     */
    public boolean log(Long operatorId, String operationType, String targetType,
                       Long targetId, String detail) {
        OperationLog log = new OperationLog();
        log.setOperatorId(operatorId);
        log.setOperationType(operationType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetail(detail);
        // created_at 由 MyBatis-Plus 自动填充（通过 @TableField(fill = FieldFill.INSERT)）
        return this.save(log);
    }

    /**
     * 记录操作日志（简化版，只记录操作人和详情）
     *
     * @param operatorId 操作人ID
     * @param detail 操作说明
     */
    public boolean logSimple(Long operatorId, String detail) {
        return log(operatorId, "GENERAL", "GENERAL", null, detail);
    }

    // ========== 查询方法 ==========

    /**
     * 查询某个管理员的所有操作日志（按时间倒序）
     *
     * @param operatorId 管理员ID
     * @return 日志列表
     */
    public List<OperationLog> getLogsByOperator(Long operatorId) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationLog::getOperatorId, operatorId)
                .orderByDesc(OperationLog::getCreatedAt);
        return this.list(wrapper);
    }

    /**
     * 分页查询所有操作日志（管理员后台使用）
     *
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页结果
     */
    public Page<OperationLog> getLogsPage(int pageNum, int pageSize) {
        Page<OperationLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(OperationLog::getCreatedAt);
        return this.page(page, wrapper);
    }

    /**
     * 按操作类型查询日志
     *
     * @param operationType 操作类型
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    public Page<OperationLog> getLogsByType(String operationType, int pageNum, int pageSize) {
        Page<OperationLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationLog::getOperationType, operationType)
                .orderByDesc(OperationLog::getCreatedAt);
        return this.page(page, wrapper);
    }

    /**
     * 查询最近的N条操作日志
     *
     * @param limit 条数限制
     * @return 日志列表
     */
    public List<OperationLog> getLatestLogs(int limit) {
        // 直接使用 LambdaQueryWrapper，不加条件
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(OperationLog::getCreatedAt)
                .last("LIMIT " + limit);
        return this.list(wrapper);
    }

    /**
     * 查询某个管理员最近的N条操作日志
     *
     * @param operatorId 管理员ID
     * @param limit 条数限制
     * @return 日志列表
     */
    public List<OperationLog> getLatestLogsByOperator(Long operatorId, int limit) {
        return this.baseMapper.selectLatestByOperatorId(operatorId, limit);
    }
}