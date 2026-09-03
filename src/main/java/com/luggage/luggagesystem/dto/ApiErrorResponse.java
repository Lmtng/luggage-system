package com.luggage.luggagesystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ApiErrorResponse {

    private int status;

    private String message;

    private LocalDateTime timestamp;
}