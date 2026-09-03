package com.luggage.luggagesystem.service;

import com.luggage.luggagesystem.dto.LoginRequest;
import com.luggage.luggagesystem.dto.RegisterRequest;
import com.luggage.luggagesystem.dto.UserResponse;
import com.luggage.luggagesystem.enums.UserStatus;

import java.util.List;

public interface UserService {

    UserResponse register(RegisterRequest request);

    UserResponse login(LoginRequest request);

    UserResponse getUserById(Long userId);

    List<UserResponse> listUsers();

    boolean changeUserStatus(
            Long userId,
            UserStatus status
    );
}