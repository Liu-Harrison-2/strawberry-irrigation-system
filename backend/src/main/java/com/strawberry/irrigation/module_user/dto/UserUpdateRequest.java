package com.strawberry.irrigation.module_user.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * 用户更新请求DTO
 * 用于接收前端更新用户信息的请求数据
 */
@Data
public class UserUpdateRequest {

    @Size(max = 50, message = "真实姓名长度不能超过50字符")
    private String realName;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String remark;
}