package com.luggage.luggagesystem.service;

import com.luggage.luggagesystem.mapper.StorageOrderMapper;
import lombok.RequiredArgsConstructor;
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
 */
@Component
@RequiredArgsConstructor
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

    private final StorageOrderMapper storageOrderMapper;

    /**
     * 生成订单号
     *
     * @return 唯一订单号，如：ST202608290001
     */
    public synchronized String generateOrderNo() {
        String today = LocalDateTime.now().format(DATE_FORMATTER);
        String datePrefix = PREFIX + today;

        // 查询当天最大订单号
        String maxOrderNo = storageOrderMapper.selectMaxOrderNoByPrefix(datePrefix);

        int nextSeq = 1;
        if (maxOrderNo != null && maxOrderNo.length() >= datePrefix.length() + SEQUENCE_LENGTH) {
            String seqStr = maxOrderNo.substring(datePrefix.length());
            try {
                nextSeq = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException e) {
                nextSeq = 1;
            }
        }

        return datePrefix + String.format("%0" + SEQUENCE_LENGTH + "d", nextSeq);
    }
}