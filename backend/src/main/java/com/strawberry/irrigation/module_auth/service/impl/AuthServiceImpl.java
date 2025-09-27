package com.strawberry.irrigation.module_auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.strawberry.irrigation.common.constants.SystemConstants;
import com.strawberry.irrigation.common.exception.BusinessException;
import com.strawberry.irrigation.module_auth.dao.RefreshTokenMapper;
import com.strawberry.irrigation.module_auth.dto.AuthResponse;
import com.strawberry.irrigation.module_auth.dto.LoginRequest;
import com.strawberry.irrigation.module_auth.dto.RegisterRequest;
import com.strawberry.irrigation.module_auth.entity.RefreshToken;
import com.strawberry.irrigation.module_auth.service.AuthService;
import com.strawberry.irrigation.module_auth.utils.JwtUtil;
import com.strawberry.irrigation.module_user.dto.UserCreateRequest;
import com.strawberry.irrigation.module_user.dto.UserResponse;
import com.strawberry.irrigation.module_user.entity.User;
import com.strawberry.irrigation.module_user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 认证服务实现类
 * 
 * 通俗解释：这就像"身份管理中心"的具体工作人员，负责处理各种身份相关的具体业务
 * 专业解释：实现认证服务接口的具体业务逻辑，处理用户注册、登录、令牌管理等核心功能
 * 项目中怎么用：Spring容器管理的服务Bean，被Controller调用处理认证相关的业务请求
 * 
 * 设计理念：使用MyBatis-Plus的QueryWrapper进行数据库操作，保持与用户管理模块的一致性
 */
@Service
@Transactional
public class AuthServiceImpl implements AuthService {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private RefreshTokenMapper refreshTokenMapper;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 用户注册实现
     * 
     * 为什么这么做：确保用户信息的唯一性和安全性，密码加密存储
     * 怎么做：检查重复 → 加密密码 → 创建用户 → 返回用户信息
     * 注意点：要先检查用户名和手机号是否已存在，避免重复注册
     */
    @Override
    public AuthResponse.UserInfo register(RegisterRequest request) {
        // 1. 检查用户名是否已存在
        if (userService.isUsernameExists(request.getUsername())) {
            throw new BusinessException(SystemConstants.BUSINESS_ERROR_CODE, "用户名已存在");
        }
        
        // 2. 检查手机号是否已存在
        if (userService.isPhoneExists(request.getPhoneNumber())) {
            throw new BusinessException(SystemConstants.BUSINESS_ERROR_CODE, "手机号已被注册");
        }
        
        // 3. 创建UserCreateRequest对象
        UserCreateRequest createRequest = new UserCreateRequest();
        createRequest.setUsername(request.getUsername());
        createRequest.setPassword(request.getPassword()); // 密码会在UserService中加密
        createRequest.setEmail(request.getEmail());
        createRequest.setRealName(request.getRealName());
        createRequest.setPhoneNumber(request.getPhoneNumber());
        createRequest.setUserType(request.getUserType());
        
        // 4. 通过UserService创建用户
        UserResponse userResponse = userService.createUser(createRequest);
        
        // 5. 返回用户信息（不包含敏感数据）
        return new AuthResponse.UserInfo(
            userResponse.getId(),
            userResponse.getUsername(),
            userResponse.getRealName(),
            userResponse.getUserType(),
            userResponse.getStatus()
        );
    }
    
    /**
     * 用户登录实现
     * 
     * 为什么这么做：验证用户身份并提供访问凭证，支持无状态认证
     * 怎么做：验证密码 → 生成JWT → 创建刷新令牌 → 返回认证信息
     * 注意点：密码验证用BCrypt，要检查用户状态，生成的令牌要保存到数据库
     */
    @Override
    public AuthResponse login(LoginRequest request) {
        // 1. 根据用户名查找用户
        User user = getUserByUsername(request.getUsername());
        
        // 2. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(SystemConstants.UNAUTHORIZED_CODE, "用户名或密码错误");
        }
        
        // 3. 检查用户状态
        if (!SystemConstants.USER_STATUS_ACTIVE.equals(user.getStatus())) {
            throw new BusinessException(SystemConstants.FORBIDDEN_CODE, "账户已被禁用或未激活");
        }
        
        // 4. 生成JWT访问令牌
        String accessToken = jwtUtil.generateAccessToken(
            user.getId(), 
            user.getUsername(), 
            user.getUserType()
        );
        
        // 5. 生成并保存刷新令牌
        String refreshTokenValue = UUID.randomUUID().toString();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(7); // 7天有效期
        
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(user.getId());
        refreshToken.setTokenHash(hashToken(refreshTokenValue));
        refreshToken.setExpiresAt(expiresAt);
        refreshTokenMapper.insert(refreshToken);
        
        // 6. 构建认证响应
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
            user.getId(),
            user.getUsername(),
            user.getRealName(),
            user.getUserType(),
            user.getStatus()
        );
        
        return new AuthResponse(
            accessToken,
            refreshTokenValue,
            "Bearer",
            jwtUtil.getAccessTokenExpirationInSeconds(),
            userInfo
        );
    }
    
    /**
     * 刷新令牌实现
     * 
     * 为什么这么做：延长用户会话，提升用户体验，避免频繁登录
     * 怎么做：验证刷新令牌 → 查找用户 → 生成新访问令牌 → 返回新认证信息
     * 注意点：要检查令牌的有效性和过期时间，可以选择是否轮换刷新令牌
     * 
     * 使用MyBatis-Plus QueryWrapper替代自定义SQL查询
     */
    @Override
    public AuthResponse refreshToken(String refreshTokenValue) {
        // 1. 使用QueryWrapper查找刷新令牌
        QueryWrapper<RefreshToken> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("token", refreshTokenValue);
        RefreshToken refreshToken = refreshTokenMapper.selectOne(queryWrapper);
        
        if (refreshToken == null) {
            throw new BusinessException(SystemConstants.UNAUTHORIZED_CODE, "刷新令牌无效");
        }
        
        // 2. 检查令牌是否有效
        if (!refreshToken.isValid()) {
            throw new BusinessException(SystemConstants.UNAUTHORIZED_CODE, "刷新令牌已过期或被撤销");
        }
        
        // 3. 查找用户信息
        User user = getUserById(refreshToken.getUserId());
        
        // 4. 检查用户状态
        if (!SystemConstants.USER_STATUS_ACTIVE.equals(user.getStatus())) {
            throw new BusinessException(SystemConstants.FORBIDDEN_CODE, "账户已被禁用");
        }
        
        // 5. 生成新的访问令牌
        String newAccessToken = jwtUtil.generateAccessToken(
            user.getId(),
            user.getUsername(),
            user.getUserType()
        );
        
        // 6. 构建用户信息
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
            user.getId(),
            user.getUsername(),
            user.getRealName(),
            user.getUserType(),
            user.getStatus()
        );
        
        // 7. 返回新的认证信息（复用原刷新令牌）
        return new AuthResponse(
            newAccessToken,
            refreshTokenValue,
            "Bearer",
            jwtUtil.getAccessTokenExpirationInSeconds(),
            userInfo
        );
    }
    
    /**
     * 用户登出实现
     * 
     * 使用MyBatis-Plus UpdateWrapper替代自定义SQL更新
     */
    @Override
    public void logout(String refreshTokenValue) {
        // 使用UpdateWrapper撤销指定的刷新令牌
        UpdateWrapper<RefreshToken> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("token", refreshTokenValue)
                    .set("revoked", true);
        
        int revokedCount = refreshTokenMapper.update(null, updateWrapper);
        if (revokedCount == 0) {
            throw new BusinessException(SystemConstants.BUSINESS_ERROR_CODE, "刷新令牌不存在");
        }
    }
    
    /**
     * 撤销用户所有令牌实现
     * 
     * 使用MyBatis-Plus UpdateWrapper替代自定义SQL更新
     */
    @Override
    public void revokeAllTokens(Long userId) {
        // 使用UpdateWrapper撤销用户的所有刷新令牌
        UpdateWrapper<RefreshToken> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", userId)
                    .set("revoked", true);
        
        refreshTokenMapper.update(null, updateWrapper);
    }
    
    /**
     * 验证访问令牌实现
     */
    @Override
    public boolean validateAccessToken(String accessToken) {
        return jwtUtil.isTokenValid(accessToken) && !jwtUtil.isTokenExpired(accessToken);
    }
    
    /**
     * 辅助方法：根据用户名获取用户
     * 
     * 为什么需要：多个方法都需要根据用户名查找用户，提取为公共方法
     * 怎么做：调用UserService的getUserEntityByUsername方法
     * 注意点：如果用户不存在会抛出异常，调用方需要处理
     */
    private User getUserByUsername(String username) {
        User user = userService.getUserEntityByUsername(username);
        if (user == null) {
            throw new BusinessException(SystemConstants.UNAUTHORIZED_CODE, "用户不存在");
        }
        return user;
    }
    
    /**
     * 辅助方法：根据用户ID获取用户
     */
    private User getUserById(Long userId) {
        User user = userService.getUserEntityById(userId);
        if (user == null) {
            throw new BusinessException(SystemConstants.BUSINESS_ERROR_CODE, "用户不存在");
        }
        return user;
    }
    
    /**
     * 对令牌进行哈希处理
     * 
     * @param token 原始令牌
     * @return 哈希后的令牌
     */
    private String hashToken(String token) {
        return passwordEncoder.encode(token);
    }
}