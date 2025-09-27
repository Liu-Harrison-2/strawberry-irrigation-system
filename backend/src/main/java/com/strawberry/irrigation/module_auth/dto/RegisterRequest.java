package com.strawberry.irrigation.module_auth.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 用户注册请求DTO
 * 
 * 通俗解释：这就像填写"开户申请表"，需要提供完整的个人信息
 * 专业解释：数据传输对象(DTO)，用于接收前端注册请求的所有必要参数
 * 项目中怎么用：前端POST到/api/auth/register时，Spring会验证这些字段并转换为User实体
 */
@Data
public class RegisterRequest {
    
    /**
     * 用户名（登录用，唯一）
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;
    
    /**
     * 密码（明文，后端会进行BCrypt加密）
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20字符之间")
    private String password;
    
    /**
     * 邮箱（可选）
     */
    @Email(message = "邮箱格式不正确")
    private String email;
    
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
    private String phoneNumber;
    
    /**
     * 用户类型（ADMIN或FARMER）
     * 默认为FARMER，管理员账户需要特殊权限创建
     */
    @NotBlank(message = "用户类型不能为空")
    @Pattern(regexp = "^(ADMIN|FARMER)$", message = "用户类型只能是ADMIN或FARMER")
    private String userType = "FARMER";
}