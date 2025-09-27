package com.strawberry.irrigation.module_auth.controller;

import com.strawberry.irrigation.common.response.Result;
import com.strawberry.irrigation.module_auth.dto.AuthResponse;
import com.strawberry.irrigation.module_auth.dto.LoginRequest;
import com.strawberry.irrigation.module_auth.dto.RegisterRequest;
import com.strawberry.irrigation.module_auth.service.AuthService;
import com.strawberry.irrigation.module_auth.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * 认证控制器
 * 
 * 通俗解释：这就像"身份验证服务台"，处理用户的注册、登录、退出等请求
 * 专业解释：提供RESTful API接口，处理用户认证相关的HTTP请求，返回统一格式的响应
 * 项目中怎么用：前端调用这些接口进行用户认证操作，所有接口都返回Result<T>格式
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * 用户注册接口
     * 
     * 为什么这么做：提供用户自主注册功能，扩大系统用户基础
     * 怎么做：接收注册信息 → 调用认证服务 → 返回用户基本信息
     * 注意点：不返回敏感信息，注册成功后用户仍需登录获取令牌
     * 
     * @param request 注册请求数据
     * @return 注册成功的用户信息
     */
    @PostMapping("/register")
    public Result<AuthResponse.UserInfo> register(@Valid @RequestBody RegisterRequest request) {
        log.info("用户注册请求，用户名: {}", request.getUsername());
        
        try {
            AuthResponse.UserInfo userInfo = authService.register(request);
            log.info("用户注册成功，用户ID: {}, 用户名: {}", userInfo.getId(), userInfo.getUsername());
            return Result.success(userInfo);
        } catch (Exception e) {
            log.error("用户注册失败，用户名: {}, 错误: {}", request.getUsername(), e.getMessage());
            throw e; // 让全局异常处理器处理
        }
    }
    
    /**
     * 用户登录接口
     * 
     * 为什么这么做：验证用户身份并提供访问凭证，实现安全的无状态认证
     * 怎么做：验证用户名密码 → 生成JWT令牌 → 返回认证信息
     * 注意点：返回访问令牌和刷新令牌，访问令牌用于API调用，刷新令牌用于延长会话
     * 
     * @param request 登录请求数据
     * @return 认证信息（包含令牌和用户信息）
     */
    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("用户登录请求，用户名: {}", request.getUsername());
        
        try {
            AuthResponse authResponse = authService.login(request);
            log.info("用户登录成功，用户名: {}", request.getUsername());
            return Result.success(authResponse);
        } catch (Exception e) {
            log.error("用户登录失败，用户名: {}, 错误: {}", request.getUsername(), e.getMessage());
            throw e; // 让全局异常处理器处理
        }
    }
    
    /**
     * 刷新令牌接口
     * 
     * 为什么这么做：延长用户会话时间，提升用户体验，避免频繁重新登录
     * 怎么做：验证刷新令牌 → 生成新的访问令牌 → 返回新的认证信息
     * 注意点：只有有效的刷新令牌才能获取新的访问令牌
     * 
     * @param refreshToken 刷新令牌
     * @return 新的认证信息
     */
    @PostMapping("/refresh")
    public Result<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("刷新令牌请求");
        
        try {
            AuthResponse authResponse = authService.refreshToken(request.getRefreshToken());
            log.info("令牌刷新成功");
            return Result.success(authResponse);
        } catch (Exception e) {
            log.error("令牌刷新失败，错误: {}", e.getMessage());
            throw e; // 让全局异常处理器处理
        }
    }
    
    /**
     * 用户登出接口
     * 
     * 为什么这么做：安全地结束用户会话，撤销刷新令牌防止被滥用
     * 怎么做：撤销指定的刷新令牌 → 返回成功信息
     * 注意点：登出后刷新令牌失效，但已发出的访问令牌在过期前仍然有效
     * 
     * @param request 登出请求（包含刷新令牌）
     * @return 操作结果
     */
    @PostMapping("/logout")
    public Result<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("用户登出请求");
        
        try {
            authService.logout(request.getRefreshToken());
            log.info("用户登出成功");
            return Result.success();
        } catch (Exception e) {
            log.error("用户登出失败，错误: {}", e.getMessage());
            throw e; // 让全局异常处理器处理
        }
    }
    
    /**
     * 撤销所有令牌接口
     * 
     * 为什么这么做：当用户怀疑账户被盗用时，可以撤销所有设备的登录状态
     * 怎么做：撤销用户的所有刷新令牌 → 强制所有设备重新登录
     * 注意点：这是一个安全功能，会影响用户在所有设备上的登录状态
     * 
     * @return 操作结果
     */
    @PostMapping("/revoke-all")
    public Result<Void> revokeAllTokens() {
        log.info("撤销所有令牌请求");
        
        try {
            // 注意：这里需要从JWT中获取当前用户ID
            // 实际实现中需要通过SecurityContext或JWT解析获取用户信息
            // 暂时使用占位符，后续在Spring Security配置完成后补充
            Long currentUserId = getCurrentUserId(); // 需要实现这个方法
            
            authService.revokeAllTokens(currentUserId);
            log.info("撤销所有令牌成功，用户ID: {}", currentUserId);
            return Result.success(0, "已撤销所有设备的登录状态", null);
        } catch (Exception e) {
            log.error("撤销所有令牌失败，错误: {}", e.getMessage());
            throw e; // 让全局异常处理器处理
        }
    }
    
    /**
     * 获取当前用户ID的辅助方法
     * 
     * 为什么这么做：很多认证相关操作需要知道当前用户是谁
     * 怎么做：使用SecurityUtils工具类从Spring Security上下文中获取
     * 注意点：如果用户未认证会抛出异常，确保只在需要认证的接口中使用
     * 
     * @return 当前用户ID
     * @throws RuntimeException 如果用户未认证
     */
    private Long getCurrentUserId() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未认证");
        }
        return userId;
    }
    
    /**
     * 刷新令牌请求DTO
     * 
     * 为什么单独定义：刷新令牌和登出接口都需要这个数据结构
     * 怎么做：简单的包装类，包含刷新令牌字符串
     * 注意点：使用内部类减少文件数量，保持代码整洁
     */
    public static class RefreshTokenRequest {
        @NotBlank(message = "刷新令牌不能为空")
        private String refreshToken;
        
        public RefreshTokenRequest() {}
        
        public RefreshTokenRequest(String refreshToken) {
            this.refreshToken = refreshToken;
        }
        
        public String getRefreshToken() {
            return refreshToken;
        }
        
        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
}