package com.strawberry.irrigation.module_auth.utils;

import com.strawberry.irrigation.module_auth.security.CustomUserDetails;
import com.strawberry.irrigation.module_user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类
 * 
 * 通俗解释：这就像"身份识别器"，随时可以告诉你当前是谁在使用系统
 * 专业解释：提供获取当前认证用户信息的便捷方法，封装Spring Security上下文操作
 * 项目中怎么用：在Controller或Service中调用这些方法获取当前用户信息
 */
@Slf4j
public class SecurityUtils {

    /**
     * 获取当前认证的用户ID
     * 
     * 为什么这么做：很多业务操作需要知道是哪个用户在操作，用于权限控制和数据关联
     * 怎么做：从Spring Security上下文中获取认证信息，提取用户ID
     * 注意点：如果用户未认证会返回null，调用方需要处理这种情况
     * 
     * @return 当前用户ID，如果未认证则返回null
     */
    public static Long getCurrentUserId() {
        try {
            CustomUserDetails userDetails = getCurrentUserDetails();
            return userDetails != null ? userDetails.getUserId() : null;
        } catch (Exception e) {
            log.warn("获取当前用户ID失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取当前认证的用户名
     * 
     * @return 当前用户名，如果未认证则返回null
     */
    public static String getCurrentUsername() {
        try {
            CustomUserDetails userDetails = getCurrentUserDetails();
            return userDetails != null ? userDetails.getUsername() : null;
        } catch (Exception e) {
            log.warn("获取当前用户名失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取当前认证的用户类型
     * 
     * @return 当前用户类型，如果未认证则返回null
     */
    public static String getCurrentUserType() {
        try {
            CustomUserDetails userDetails = getCurrentUserDetails();
            return userDetails != null ? userDetails.getUserType() : null;
        } catch (Exception e) {
            log.warn("获取当前用户类型失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取当前认证的用户实体
     * 
     * @return 当前用户实体，如果未认证则返回null
     */
    public static User getCurrentUser() {
        try {
            CustomUserDetails userDetails = getCurrentUserDetails();
            return userDetails != null ? userDetails.getUser() : null;
        } catch (Exception e) {
            log.warn("获取当前用户实体失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 检查当前用户是否已认证
     * 
     * @return true表示已认证，false表示未认证
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.isAuthenticated() && 
               !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * 检查当前用户是否具有指定角色
     * 
     * @param role 角色名称（不需要ROLE_前缀）
     * @return true表示具有该角色，false表示不具有
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }

    /**
     * 检查当前用户是否为管理员
     * 
     * @return true表示是管理员，false表示不是
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * 获取当前用户详情对象
     * 
     * @return CustomUserDetails对象，如果未认证则返回null
     */
    private static CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return (CustomUserDetails) principal;
        }
        
        return null;
    }
}