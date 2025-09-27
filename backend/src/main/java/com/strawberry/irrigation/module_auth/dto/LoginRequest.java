package com.strawberry.irrigation.module_auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户登录请求DTO
 * 
 * 通俗解释：这就像填写"登录表单"，只需要用户名和密码两个必填项
 * 专业解释：数据传输对象(DTO)，用于接收前端登录请求的参数
 * 项目中怎么用：前端POST到/api/auth/login时，Spring会自动将JSON转换为这个对象
 */
@Data
public class LoginRequest {
    
    /**
     * 用户名
     * 必填，长度3-20字符
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20字符之间")
    private String username;
    
    /**
     * 密码
     * 必填，最少6位（前端传来的是明文，后端会进行BCrypt加密比较）
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度不能少于6位")
    private String password;
}