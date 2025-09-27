package com.strawberry.irrigation.module_auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证响应DTO
 * 
 * 通俗解释：这就像银行给你的"存折"，里面有你的身份证明(token)和基本信息
 * 专业解释：认证成功后返回的数据传输对象，包含JWT令牌和用户基本信息
 * 项目中怎么用：登录成功后，后端返回这个对象，前端保存token用于后续请求认证
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    /**
     * 访问令牌（JWT格式）
     * 前端需要在后续请求的Authorization头中携带：Bearer {accessToken}
     */
    private String accessToken;
    
    /**
     * 刷新令牌（用于获取新的访问令牌）
     * 当accessToken过期时，可以用这个token获取新的accessToken
     */
    private String refreshToken;
    
    /**
     * 令牌类型（固定为"Bearer"）
     */
    private String tokenType = "Bearer";
    
    /**
     * 访问令牌过期时间（秒）
     */
    private Long expiresIn;
    
    /**
     * 用户基本信息
     */
    private UserInfo userInfo;
    
    /**
     * 用户信息内嵌类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String realName;
        private String userType;
        private String status;
    }
}