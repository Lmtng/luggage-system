package com.luggage.luggagesystem.dto;

import com.luggage.luggagesystem.enums.UserRole;
import com.luggage.luggagesystem.enums.UserStatus;
import lombok.Data;

@Data
public class UserResponse {

    private Long id;

    private String username;

    private String nickname;

    private UserRole role;

    private UserStatus status;
}