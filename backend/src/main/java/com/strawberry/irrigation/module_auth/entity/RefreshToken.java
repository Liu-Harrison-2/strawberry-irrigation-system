package com.strawberry.irrigation.module_auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

/**
 * 刷新令牌实体类
 * 
 * 通俗解释：这就像"长期有效的身份证"，用于换取新的访问令牌
 * 专业解释：存储JWT刷新令牌的相关信息，包括令牌哈希、过期时间、撤销状态等
 * 项目中怎么用：用户登录后生成refresh token，后续可用此token获取新的access token
 * 
 * 设计要点：
 * 1. 存储令牌哈希而不是明文，提高安全性
 * 2. 支持令牌撤销，可以主动使令牌失效
 * 3. 记录设备信息和IP，便于安全审计
 * 4. 支持批量撤销（如用户修改密码时）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("refresh_tokens")
public class RefreshToken {
    
    /**
     * 令牌ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 令牌哈希值（SHA-256）
     * 
     * 为什么存储哈希：安全考虑，即使数据库泄露也无法直接获取原始令牌
     * 怎么做：使用SHA-256对原始令牌进行哈希处理后存储
     * 注意点：查询时需要对输入令牌进行相同的哈希处理
     */
    @TableField("token_hash")
    @NotBlank(message = "令牌哈希不能为空")
    private String tokenHash;

    /**
     * 关联用户ID
     */
    @TableField("user_id")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 令牌过期时间
     * 
     * 为什么需要：刷新令牌也有有效期，通常比访问令牌长（如7天）
     * 怎么做：在生成令牌时设置过期时间，验证时检查是否过期
     */
    @TableField("expires_at")
    @NotNull(message = "过期时间不能为空")
    private OffsetDateTime expiresAt;

    /**
     * 是否已撤销
     * 
     * 为什么需要：支持主动撤销令牌，如用户登出、修改密码等场景
     * 怎么做：设置为true表示令牌已失效，验证时检查此字段
     */
    @TableField("is_revoked")
    private Boolean isRevoked = false;

    /**
     * 设备信息
     * 
     * 用途：记录令牌来源设备，便于用户管理和安全审计
     * 格式：如"iPhone 14 Pro"、"Chrome on Windows"等
     */
    @TableField("device_info")
    private String deviceInfo;

    /**
     * IP地址
     * 
     * 用途：安全审计，检测异常登录行为
     * 格式：IPv4或IPv6地址
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * 用户代理信息
     * 
     * 用途：记录客户端详细信息，便于问题排查和安全分析
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;

    /**
     * 撤销时间
     * 
     * 用途：记录令牌被撤销的具体时间，便于审计
     */
    @TableField("revoked_at")
    private OffsetDateTime revokedAt;

    /**
     * 撤销原因
     * 
     * 用途：记录令牌撤销的原因，便于问题分析
     * 常见值：USER_LOGOUT（用户登出）、PASSWORD_CHANGED（密码修改）、
     *        SECURITY_BREACH（安全问题）、ADMIN_REVOKE（管理员撤销）等
     */
    @TableField("revoked_reason")
    private String revokedReason;

    // ========== 便利构造方法 ==========

    /**
     * 创建新的刷新令牌
     * 
     * @param tokenHash 令牌哈希
     * @param userId 用户ID
     * @param expiresAt 过期时间
     * @param deviceInfo 设备信息
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     */
    public RefreshToken(String tokenHash, Long userId, OffsetDateTime expiresAt, 
                       String deviceInfo, String ipAddress, String userAgent) {
        this.tokenHash = tokenHash;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.deviceInfo = deviceInfo;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.isRevoked = false;
    }

    // ========== 业务方法 ==========

    /**
     * 检查令牌是否有效
     * 
     * @return true表示有效，false表示无效
     */
    public boolean isValid() {
        return !isRevoked && expiresAt.isAfter(OffsetDateTime.now());
    }

    /**
     * 撤销令牌
     * 
     * @param reason 撤销原因
     */
    public void revoke(String reason) {
        this.isRevoked = true;
        this.revokedAt = OffsetDateTime.now();
        this.revokedReason = reason;
    }

    /**
     * 检查令牌是否过期
     * 
     * @return true表示已过期，false表示未过期
     */
    public boolean isExpired() {
        return expiresAt.isBefore(OffsetDateTime.now());
    }
}