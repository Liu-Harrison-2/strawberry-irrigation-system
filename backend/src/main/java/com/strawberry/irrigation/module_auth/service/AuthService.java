package com.strawberry.irrigation.module_auth.service;

import com.strawberry.irrigation.module_auth.dto.AuthResponse;
import com.strawberry.irrigation.module_auth.dto.LoginRequest;
import com.strawberry.irrigation.module_auth.dto.RegisterRequest;

/**
 * 认证服务接口
 * 
 * 通俗解释：这就像"身份管理中心"的服务窗口，提供开户(注册)、验证身份(登录)、换证(刷新令牌)等服务
 * 专业解释：定义认证授权模块的核心业务操作接口，包括用户注册、登录、令牌管理等功能
 * 项目中怎么用：Controller层调用这些方法实现认证功能，Service实现类处理具体的业务逻辑
 */
public interface AuthService {
    
    /**
     * 用户注册
     * 
     * 为什么需要：新用户需要创建账户才能使用系统
     * 做什么：验证用户信息 → 加密密码 → 保存到数据库 → 返回注册结果
     * 注意点：要检查用户名和手机号是否已存在，密码必须BCrypt加密
     * 
     * @param request 注册请求信息
     * @return 注册成功的用户信息（不包含敏感数据）
     * @throws BusinessException 当用户名已存在或其他业务错误时抛出
     */
    AuthResponse.UserInfo register(RegisterRequest request);
    
    /**
     * 用户登录
     * 
     * 为什么需要：验证用户身份并颁发访问凭证
     * 做什么：验证用户名密码 → 生成JWT令牌 → 保存刷新令牌 → 返回认证信息
     * 注意点：密码验证用BCrypt.matches()，要检查用户状态是否为ACTIVE
     * 
     * @param request 登录请求信息
     * @return 认证响应，包含访问令牌、刷新令牌和用户信息
     * @throws BusinessException 当用户名不存在、密码错误或账户被禁用时抛出
     */
    AuthResponse login(LoginRequest request);
    
    /**
     * 刷新访问令牌
     * 
     * 为什么需要：访问令牌过期时，用刷新令牌获取新的访问令牌，避免用户重新登录
     * 做什么：验证刷新令牌 → 生成新的访问令牌 → 可选择性更新刷新令牌
     * 注意点：要检查刷新令牌是否有效、未过期、未被撤销
     * 
     * @param refreshToken 刷新令牌
     * @return 新的认证响应，包含新的访问令牌
     * @throws BusinessException 当刷新令牌无效或过期时抛出
     */
    AuthResponse refreshToken(String refreshToken);
    
    /**
     * 用户登出
     * 
     * 为什么需要：撤销用户的令牌，确保安全性
     * 做什么：撤销指定的刷新令牌 → 可选择性撤销所有令牌
     * 注意点：JWT访问令牌无法主动撤销（无状态特性），只能等待过期
     * 
     * @param refreshToken 要撤销的刷新令牌
     * @throws BusinessException 当令牌不存在时抛出
     */
    void logout(String refreshToken);
    
    /**
     * 撤销用户的所有令牌
     * 
     * 为什么需要：安全原因需要强制用户在所有设备上重新登录
     * 做什么：撤销指定用户的所有刷新令牌
     * 注意点：通常用于密码重置、账户安全事件等场景
     * 
     * @param userId 用户ID
     */
    void revokeAllTokens(Long userId);
    
    /**
     * 验证访问令牌
     * 
     * 为什么需要：某些场景需要主动验证令牌的有效性
     * 做什么：解析令牌 → 验证签名和过期时间 → 返回验证结果
     * 注意点：这个方法主要用于调试或特殊业务场景
     * 
     * @param accessToken 访问令牌
     * @return 令牌是否有效
     */
    boolean validateAccessToken(String accessToken);
}