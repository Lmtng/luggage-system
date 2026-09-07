package com.luggage.luggagesystem.service;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 订单号生成器
 *
 * 格式：ST + 日期 + 序列号
 * 例如：ST202608290001
 *
 * 设计思想：
 * - 日期部分：YYYYMMDD，便于按日期查询
 * - 序列号：4位数字，从0001开始，每天重置
 * - 保证了订单号的可读性和唯一性
 *
 * @author 成员B
 * @date 2026-09-01
 */
@Component
public class OrderNoGenerator {

    /**
     * 日期格式化器
     */
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 订单号前缀
     */
    private static final String PREFIX = "ST";

    /**
     * 序列号长度
     */
    private static final int SEQUENCE_LENGTH = 4;

    /**
     * 序列号计数器（每天重置）
     */
    private final AtomicInteger sequence = new AtomicInteger(0);

    /**
     * 当前日期（用于判断是否重置序列号）
     */
    private String currentDate = "";

    /**
     * 生成订单号
     *
     * @return 唯一订单号，如：ST202608290001
     */
    public synchronized String generateOrderNo() {
        // 获取当前日期
        String today = LocalDateTime.now().format(DATE_FORMATTER);

        // 如果日期变了，重置序列号
        if (!today.equals(currentDate)) {
            currentDate = today;
            sequence.set(0);
        }

        // 序列号加1
        int seq = sequence.incrementAndGet();

        // 格式化序列号为4位数字
        String seqStr = String.format("%0" + SEQUENCE_LENGTH + "d", seq);

        // 组装订单号
        return PREFIX + currentDate + seqStr;
    }
}