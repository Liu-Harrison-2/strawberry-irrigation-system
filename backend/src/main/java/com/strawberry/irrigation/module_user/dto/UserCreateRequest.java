package com.strawberry.irrigation.module_user.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * 用户创建请求DTO
 * 用于接收前端创建用户的请求数据
 */
@Data
public class UserCreateRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20字符之间")
    private String password;

    @Email(message = "邮箱格式不正确")
    private String email; // 可选字段

    @Size(max = 50, message = "真实姓名长度不能超过50字符")
    private String realName;

    @Pattern(regexp = "^1[3-9]\\d{9}$|^$", message = "手机号格式不正确")
    private String phoneNumber;

    @NotBlank(message = "用户类型不能为空")
    private String userType;

    private String remark;
}