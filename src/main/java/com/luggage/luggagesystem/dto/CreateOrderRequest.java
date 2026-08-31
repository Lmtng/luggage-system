package com.luggage.luggagesystem.dto;

import lombok.Data;

@Data
public class CreateOrderRequest {
    private Long cellId;  // 用户选择的柜格ID
}