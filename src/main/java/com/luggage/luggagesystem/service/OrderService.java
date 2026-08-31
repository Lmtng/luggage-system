package com.luggage.luggagesystem.service;

import com.luggage.luggagesystem.dto.CreateOrderRequest;
import com.luggage.luggagesystem.dto.OrderVO;

public interface OrderService {
    OrderVO createOrder(Long userId, CreateOrderRequest request);
}