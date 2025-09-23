package com.strawberry.irrigation.module_user.entity;

import com.strawberry.irrigation.common.constants.SystemConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 定义用户的基本信息和业务属性
 */
@Data
@NoArgsConstructor // 添加无参构造方法
@AllArgsConstructor  // 添加全参构造方法
public class User {

    /**
     * 用户ID（主键）
     */
    private Long id;

    /**
     * 用户名（登录用，唯一）
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    /**
     * 真实姓名
     */
    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名长度不能超过50字符")
    private String realName;

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 用户类型（ADMIN-管理员，FARMER-农户）
     */
    @NotBlank(message = "用户类型不能为空")
    private String userType;

    /**
     * 用户状态（ACTIVE-正常，INACTIVE-未激活，BANNED-禁用）
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 备注信息
     */
    private String remark;

    // 构造方法：创建新用户时使用（默认为未激活状态）
    public User(String username, String realName, String phone, String email, String userType) {
        this.username = username;
        this.realName = realName;
        this.phone = phone;
        this.email = email;
        this.userType = userType;
        this.status = SystemConstants.USER_STATUS_INACTIVE; // 初始状态为未激活状态
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
}