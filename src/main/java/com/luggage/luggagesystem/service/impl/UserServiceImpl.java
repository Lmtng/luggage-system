package com.luggage.luggagesystem.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.luggage.luggagesystem.dto.LoginRequest;
import com.luggage.luggagesystem.dto.RegisterRequest;
import com.luggage.luggagesystem.dto.UserResponse;
import com.luggage.luggagesystem.entity.SysUser;
import com.luggage.luggagesystem.enums.UserRole;
import com.luggage.luggagesystem.enums.UserStatus;
import com.luggage.luggagesystem.mapper.SysUserMapper;
import com.luggage.luggagesystem.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.List;
@Service
public class UserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            SysUserMapper sysUserMapper,
            PasswordEncoder passwordEncoder) {

        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        validateRegisterRequest(request);

        String username = request.getUsername().trim();

        Long existingCount = sysUserMapper.selectCount(
                Wrappers.<SysUser>lambdaQuery()
                        .eq(SysUser::getUsername, username)
        );

        if (existingCount > 0) {
            throw new IllegalArgumentException("用户名已经存在");
        }

        String nickname = username;

        if (StringUtils.hasText(request.getNickname())) {
            nickname = request.getNickname().trim();
        }

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPasswordHash(
                passwordEncoder.encode(request.getPassword())
        );
        user.setNickname(nickname);

        // 普通注册不能自行成为管理员
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.NORMAL);

        sysUserMapper.insert(user);

        return toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse login(LoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("登录信息不能为空");
        }

        if (!StringUtils.hasText(request.getUsername())) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        if (!StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("密码不能为空");
        }

        String username = request.getUsername().trim();

        SysUser user = sysUserMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery()
                        .eq(SysUser::getUsername, username)
        );

        // 用户不存在和密码错误使用相同提示，避免泄露账号信息
        if (user == null
                || !passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        )) {

            throw new IllegalArgumentException(
                    "用户名或密码错误"
            );
        }

        if (user.getStatus() == UserStatus.DISABLED) {
            throw new IllegalStateException("该账号已被停用");
        }

        return toResponse(user);
    }

    private void validateRegisterRequest(
            RegisterRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("注册信息不能为空");
        }

        if (!StringUtils.hasText(request.getUsername())) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        String username = request.getUsername().trim();

        if (username.length() < 3
                || username.length() > 30) {

            throw new IllegalArgumentException(
                    "用户名长度必须在3到30个字符之间"
            );
        }

        if (!username.matches("[A-Za-z0-9_]+")) {
            throw new IllegalArgumentException(
                    "用户名只能包含字母、数字和下划线"
            );
        }

        if (!StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("密码不能为空");
        }

        if (request.getPassword().length() < 6
                || request.getPassword().length() > 20) {

            throw new IllegalArgumentException(
                    "密码长度必须在6到20个字符之间"
            );
        }

        if (StringUtils.hasText(request.getNickname())
                && request.getNickname().trim().length() > 30) {

            throw new IllegalArgumentException(
                    "昵称不能超过30个字符"
            );
        }
    }

    private UserResponse toResponse(SysUser user) {
        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());

        return response;
    }
    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        if (userId == null) {
            return null;
        }

        SysUser user = sysUserMapper.selectById(userId);

        if (user == null) {
            return null;
        }

        return toResponse(user);
    }
    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return sysUserMapper.selectList(
                        Wrappers.<SysUser>lambdaQuery()
                                .orderByAsc(SysUser::getId)
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public boolean changeUserStatus(
            Long userId,
            UserStatus status) {

        if (userId == null || status == null) {
            throw new IllegalArgumentException(
                    "用户编号和状态不能为空"
            );
        }

        SysUser user = sysUserMapper.selectById(userId);

        if (user == null) {
            return false;
        }

        // 状态没有变化时，也视为操作成功
        if (user.getStatus() == status) {
            return true;
        }

        int affectedRows = sysUserMapper.update(
                null,
                Wrappers.<SysUser>lambdaUpdate()
                        .eq(SysUser::getId, userId)
                        .set(SysUser::getStatus, status)
        );

        return affectedRows == 1;
    }
}
