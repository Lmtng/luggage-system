package com.luggage.luggagesystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.luggage.luggagesystem.dto.CreateOrderRequest;
import com.luggage.luggagesystem.dto.OrderVO;
import com.luggage.luggagesystem.entity.LockerCell;
import com.luggage.luggagesystem.entity.StorageOrder;
import com.luggage.luggagesystem.entity.User;
import com.luggage.luggagesystem.mapper.LockerCellMapper;
import com.luggage.luggagesystem.mapper.StorageOrderMapper;
import com.luggage.luggagesystem.mapper.UserMapper;
import com.luggage.luggagesystem.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final StorageOrderMapper storageOrderMapper;
    private final LockerCellMapper lockerCellMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public OrderVO createOrder(Long userId, CreateOrderRequest request) {
        // 1. 查询柜格是否存在
        LockerCell cell = lockerCellMapper.selectById(request.getCellId());
        if (cell == null) {
            throw new RuntimeException("柜格不存在");
        }

        // 2. 乐观锁占用柜格（状态必须为AVAILABLE）
        UpdateWrapper<LockerCell> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", request.getCellId())
                .eq("status", "AVAILABLE")
                .eq("version", cell.getVersion());
        updateWrapper.set("status", "OCCUPIED")
                .set("version", cell.getVersion() + 1);

        int updated = lockerCellMapper.update(null, updateWrapper);
        if (updated == 0) {
            throw new RuntimeException("柜格已被占用，请重新选择");
        }

        // 3. 生成订单号和取件码
        String orderNo = generateOrderNo();
        String pickupCode = generatePickupCode();

        // 4. 创建订单
        StorageOrder order = new StorageOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setCellId(request.getCellId());
        order.setPickupCodeHash(pickupCode);  // 实际应该存摘要，暂时存明文方便测试
        order.setStartTime(LocalDateTime.now());
        order.setStatus("STORED");
        order.setPaymentStatus("UNPAID");
        order.setAmount(java.math.BigDecimal.ZERO);

        storageOrderMapper.insert(order);

        // 5. 构造返回数据
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(orderNo);
        vo.setCellId(cell.getId());
        vo.setCellNo(cell.getCellNo());
        vo.setSizeType(cell.getSizeType());
        vo.setLocation("测试位置");  // 暂时写死，后面可以关联查询
        vo.setPickupCode(pickupCode);
        vo.setStartTime(order.getStartTime());
        vo.setStatus(order.getStatus());
        vo.setAmount(order.getAmount());

        return vo;
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = new Random().nextInt(10000);
        return "ST" + timestamp + String.format("%04d", random);
    }

    private String generatePickupCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}