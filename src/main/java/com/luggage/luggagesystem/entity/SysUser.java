package com.luggage.luggagesystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.luggage.luggagesystem.enums.UserRole;
import com.luggage.luggagesystem.enums.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;
/**
 * 系统用户实体，对应sys_user表。
 */
@Data
@TableName("sys_user")
public class SysUser {
    /**
     * 用户主键。
     */
    @TableId(type=IdType.AUTO)
    private Long id;
    /**
     * 登录用户名。
     */
    private String username;
    /**
     * 加密后的密码摘要。
     */
    private String passwordHash;
    /**
     * 用户昵称。
     */
    private String nickname;
    /**
     * 用户角色。
     */
    private UserRole role;
    /**
     * 用户状态。
     */
    private UserStatus status;
    /**
     * 创建时间。
     */
    private LocalDateTime  createdAt;
    /**
     * 更新时间。
     */
    private LocalDateTime updatedAt;
}
