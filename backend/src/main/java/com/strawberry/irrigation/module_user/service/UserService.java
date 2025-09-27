package com.strawberry.irrigation.module_user.service;

import com.strawberry.irrigation.module_user.dto.UserCreateRequest;
import com.strawberry.irrigation.module_user.dto.UserResponse;
import com.strawberry.irrigation.module_user.dto.UserUpdateRequest;
import com.strawberry.irrigation.module_user.entity.User;

import java.util.List;

/**
 * 用户服务接口
 * 定义用户管理的核心业务操作
 */
public interface UserService {

    /**
     * 创建新用户
     * @param request 用户创建请求
     * @return 创建成功的用户信息
     */
    UserResponse createUser(UserCreateRequest request);

    /**
     * 根据ID获取用户信息
     * @param id 用户ID
     * @return 用户信息
     */
    UserResponse getUserById(Long id);

    /**
     * 根据用户名获取用户信息
     * @param username 用户名
     * @return 用户信息
     */
    UserResponse getUserByUsername(String username);

    /**
     * 更新用户信息
     * @param id 用户ID
     * @param request 用户更新请求
     * @return 更新后的用户信息
     */
    UserResponse updateUser(Long id, UserUpdateRequest request);

    /**
     * 删除用户
     * @param id 用户ID
     */
    void deleteUser(Long id);

    /**
     * 获取所有用户列表
     * @return 用户列表
     */
    List<UserResponse> getAllUsers();

    /**
     * 分页获取用户列表
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 用户列表
     */
    List<UserResponse> getUserPage(int page, int size);

    /**
     * 根据用户类型获取用户列表
     * @param userType 用户类型
     * @return 用户列表
     */
    List<UserResponse> getUsersByType(String userType);

    /**
     * 获取用户总数
     * @return 用户总数
     */
    long getUserCount();

    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean isUsernameExists(String username);

    /**
     * 检查手机号是否已存在
     * @param phone 手机号
     * @return 是否存在
     */
    boolean isPhoneExists(String phone);

    // ========== 认证服务专用方法 ==========
    
    /**
     * 根据用户名获取用户实体（供认证服务使用）
     * @param username 用户名
     * @return 用户实体，如果不存在返回null
     */
    User getUserEntityByUsername(String username);
    
    /**
     * 根据用户ID获取用户实体（供认证服务使用）
     * @param id 用户ID
     * @return 用户实体，如果不存在返回null
     */
    User getUserEntityById(Long id);
    
    /**
     * 根据用户名或邮箱获取用户实体（供认证服务使用）
     * @param usernameOrEmail 用户名或邮箱
     * @return 用户实体，如果不存在返回null
     */
    User findByUsernameOrEmail(String usernameOrEmail);
}