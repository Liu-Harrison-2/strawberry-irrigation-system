package com.strawberry.irrigation.module_auth.security;

import com.strawberry.irrigation.common.constants.SystemConstants;
import com.strawberry.irrigation.module_user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * 自定义用户详情类
 * 
 * 通俗解释：这就像"用户身份证"，包含了Spring Security需要的所有用户信息
 * 专业解释：实现Spring Security的UserDetails接口，将系统用户信息适配到Spring Security框架
 * 项目中怎么用：JWT过滤器验证令牌后，创建这个对象设置到SecurityContext中
 */
public class CustomUserDetails implements UserDetails {
    
    private final User user;
    
    /**
     * 构造函数
     * 
     * @param user 系统用户实体
     */
    public CustomUserDetails(User user) {
        this.user = user;
    }
    
    /**
     * 获取用户权限集合
     * 
     * 为什么这么做：Spring Security需要知道用户有哪些权限，用于方法级和URL级的权限控制
     * 怎么做：根据用户类型返回对应的权限，目前只有ADMIN和FARMER两种类型
     * 注意点：权限名称要与@PreAuthorize和SecurityConfig中的配置保持一致，hasRole()需要ROLE_前缀
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 根据用户类型分配权限，添加ROLE_前缀以匹配Spring Security的hasRole()要求
        String authority = "ROLE_" + user.getUserType();
        return Collections.singletonList(new SimpleGrantedAuthority(authority));
    }
    
    /**
     * 获取密码
     * 
     * 注意：JWT认证中不需要密码验证，但接口要求必须实现
     */
    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }
    
    /**
     * 获取用户名
     * 
     * 注意：这里返回的是用户名，Spring Security用它来标识用户
     */
    @Override
    public String getUsername() {
        return user.getUsername();
    }
    
    /**
     * 账户是否未过期
     * 
     * 为什么这么做：Spring Security需要知道账户状态，决定是否允许认证
     * 怎么做：目前系统没有账户过期机制，直接返回true
     * 注意点：如果将来需要账户过期功能，可以在User实体中添加过期时间字段
     */
    @Override
    public boolean isAccountNonExpired() {
        return true; // 账户永不过期
    }
    
    /**
     * 账户是否未被锁定
     * 
     * 为什么这么做：支持账户锁定功能，提高系统安全性
     * 怎么做：检查用户状态，如果是BANNED则认为被锁定
     * 注意点：被锁定的用户无法登录，即使密码正确
     */
    @Override
    public boolean isAccountNonLocked() {
        return !SystemConstants.USER_STATUS_BANNED.equals(user.getStatus());
    }
    
    /**
     * 凭证（密码）是否未过期
     * 
     * 为什么这么做：支持密码过期功能，强制用户定期更换密码
     * 怎么做：目前系统没有密码过期机制，直接返回true
     * 注意点：如果将来需要密码过期功能，可以在User实体中添加密码更新时间字段
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 密码永不过期
    }
    
    /**
     * 账户是否启用
     * 
     * 为什么这么做：支持账户启用/禁用功能，管理员可以临时禁用用户
     * 怎么做：检查用户状态，只有ACTIVE状态的用户才被认为是启用的
     * 注意点：禁用的用户无法登录，需要管理员重新启用
     */
    @Override
    public boolean isEnabled() {
        return SystemConstants.USER_STATUS_ACTIVE.equals(user.getStatus());
    }
    
    /**
     * 获取原始用户实体
     * 
     * 为什么需要：有时候需要访问用户的完整信息，如ID、真实姓名等
     * 怎么做：提供getter方法，返回包装的User实体
     * 注意点：这不是UserDetails接口的方法，是我们自定义的便利方法
     */
    public User getUser() {
        return user;
    }
    
    /**
     * 获取用户ID
     * 
     * 为什么需要：很多业务操作需要用户ID，提供便利方法
     */
    public Long getUserId() {
        return user.getId();
    }
    
    /**
     * 获取用户类型
     * 
     * 为什么需要：权限控制和业务逻辑中经常需要判断用户类型
     */
    public String getUserType() {
        return user.getUserType();
    }
    
    /**
     * 获取真实姓名
     * 
     * 为什么需要：显示用户信息时需要真实姓名
     */
    public String getRealName() {
        return user.getRealName();
    }
}