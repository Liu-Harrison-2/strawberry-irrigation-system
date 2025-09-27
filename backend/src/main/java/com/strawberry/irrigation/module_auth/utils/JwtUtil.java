package com.strawberry.irrigation.module_auth.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 
 * 通俗解释：这就像一个"证件制作机"，可以制作身份证(生成token)、验证身份证真伪(验证token)、读取身份证信息(解析token)
 * 专业解释：基于JJWT库实现的JWT令牌工具类，提供令牌的生成、解析、验证等核心功能
 * 项目中怎么用：登录时生成token，每次请求时验证token，从token中提取用户信息
 */
@Component
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.issuer}")
    private String issuer;
    
    @Value("${jwt.access-token.expires-in-minutes}")
    private int accessTokenExpiresInMinutes;
    
    @Value("${jwt.refresh-token.expires-in-days}")
    private int refreshTokenExpiresInDays;
    
    /**
     * 获取签名密钥
     * 
     * 为什么这么做：JWT需要用密钥签名来防止篡改，就像公章一样
     * 怎么做：将配置的字符串密钥转换为加密算法需要的SecretKey对象
     * 注意点：密钥长度必须足够长，生产环境要用环境变量
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
    
    /**
     * 生成访问令牌
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @param userType 用户类型
     * @return JWT访问令牌
     */
    public String generateAccessToken(Long userId, String username, String userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("userType", userType);
        claims.put("tokenType", "access");
        
        return createToken(claims, username, accessTokenExpiresInMinutes * 60 * 1000L);
    }
    
    /**
     * 生成刷新令牌
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @return JWT刷新令牌
     */
    public String generateRefreshToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("tokenType", "refresh");
        
        return createToken(claims, username, refreshTokenExpiresInDays * 24 * 60 * 60 * 1000L);
    }
    
    /**
     * 创建JWT令牌
     * 
     * 为什么这么做：统一的令牌创建逻辑，避免重复代码
     * 怎么做：设置声明(claims)、主题(subject)、签发者、过期时间，然后签名
     * 注意点：过期时间是毫秒级别的时间戳
     */
    private String createToken(Map<String, Object> claims, String subject, long expirationTimeInMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTimeInMs);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    /**
     * 从令牌中提取用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }
    
    /**
     * 从令牌中提取用户ID
     */
    public Long getUserIdFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("userId", Long.class));
    }
    
    /**
     * 从令牌中提取用户类型
     */
    public String getUserTypeFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("userType", String.class));
    }
    
    /**
     * 从令牌中提取过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }
    
    /**
     * 从令牌中提取指定声明
     * 
     * 为什么这么做：提供通用的声明提取方法，支持不同类型的数据提取
     * 怎么做：解析token获取所有声明，然后用函数式接口提取特定声明
     * 注意点：如果token无效会抛出异常，需要在调用处处理
     */
    public <T> T getClaimFromToken(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * 从令牌中提取所有声明
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * 检查令牌是否过期
     */
    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
    
    /**
     * 验证令牌
     * 
     * 为什么这么做：确保token是有效的、未过期的、属于指定用户的
     * 怎么做：提取token中的用户名与传入的用户名比较，同时检查是否过期
     * 注意点：这里只做基础验证，复杂的权限验证在Security配置中处理
     */
    public Boolean validateToken(String token, String username) {
        try {
            final String tokenUsername = getUsernameFromToken(token);
            return (username.equals(tokenUsername) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 验证令牌格式和签名
     * 
     * 为什么这么做：在不知道用户名的情况下，先验证token的基本有效性
     * 怎么做：尝试解析token，如果成功说明格式正确且签名有效
     * 注意点：这个方法不检查过期时间，只检查格式和签名
     */
    public Boolean isTokenValid(String token) {
        try {
            getAllClaimsFromToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 获取访问令牌过期时间（秒）
     */
    public long getAccessTokenExpirationInSeconds() {
        return accessTokenExpiresInMinutes * 60L;
    }
}