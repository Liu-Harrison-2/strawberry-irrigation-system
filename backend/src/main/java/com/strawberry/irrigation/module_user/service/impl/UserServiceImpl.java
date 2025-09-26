package com.strawberry.irrigation.module_user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.strawberry.irrigation.common.constants.SystemConstants;
import com.strawberry.irrigation.common.exception.BusinessException;
import com.strawberry.irrigation.module_user.dao.UserMapper;
import com.strawberry.irrigation.module_user.dto.UserCreateRequest;
import com.strawberry.irrigation.module_user.dto.UserResponse;
import com.strawberry.irrigation.module_user.dto.UserUpdateRequest;
import com.strawberry.irrigation.module_user.entity.User;
import com.strawberry.irrigation.module_user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 * 使用MyBatis-Plus的QueryWrapper实现查询，充分利用MP的自动查询功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    @Transactional //覆盖默认只读事务，使用读写事务
    public UserResponse createUser(UserCreateRequest request) {
        log.info("开始创建用户，用户名: {}", request.getUsername());

        // 1. 校验用户类型
        validateCreateRequest(request);

        // 2. 检查用户名是否已存在 - 使用MP的QueryWrapper
        QueryWrapper<User> usernameQuery = new QueryWrapper<>();
        usernameQuery.eq("username", request.getUsername());
        if (userMapper.selectCount(usernameQuery) > 0) {
            throw new BusinessException(SystemConstants.BUSINESS_ERROR_CODE,
                    "用户名 '" + request.getUsername() + "' 已存在");
        }

        // 3. 检查邮箱是否已存在（如果提供了邮箱）- 使用MP的QueryWrapper
        if (StringUtils.hasText(request.getEmail())) {
            QueryWrapper<User> emailQuery = new QueryWrapper<>();
            emailQuery.eq("email", request.getEmail());
            if (userMapper.selectCount(emailQuery) > 0) {
                throw new BusinessException(SystemConstants.BUSINESS_ERROR_CODE,
                        "邮箱 '" + request.getEmail() + "' 已存在");
            }
        }

        // 4. 检查手机号是否已存在（如果提供了手机号）- 使用MP的QueryWrapper
        if (StringUtils.hasText(request.getPhoneNumber())) {
            QueryWrapper<User> phoneQuery = new QueryWrapper<>();
            phoneQuery.eq("phone_number", request.getPhoneNumber());
            if (userMapper.selectCount(phoneQuery) > 0) {
                throw new BusinessException(SystemConstants.BUSINESS_ERROR_CODE,
                        "手机号 '" + request.getPhoneNumber() + "' 已存在");
            }
        }

        // 5. 创建用户实体
        User user = new User(
                request.getUsername(),
                request.getPassword(), // 后续需要加密
                request.getEmail(),
                request.getRealName(),
                request.getPhoneNumber(),
                request.getUserType()
        );
        user.setRemark(request.getRemark());

        // 6. 保存用户（MyBatis-Plus会自动填充创建时间和更新时间）
        userMapper.insert(user);

        log.info("用户创建成功，ID: {}, 用户名: {}", user.getId(), user.getUsername());
        return new UserResponse(user);
    }

    @Override
    public UserResponse getUserById(Long id) {
        log.info("根据ID查询用户: {}", id);

        // 使用MP的selectById方法
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(SystemConstants.BUSINESS_ERROR_CODE,
                    SystemConstants.USER_NOT_FOUND);
        }

        return new UserResponse(user);
    }

    @Override
    public UserResponse getUserByUsername(String username) {
        log.info("根据用户名查询用户: {}", username);

        // 使用MP的QueryWrapper查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new BusinessException(SystemConstants.BUSINESS_ERROR_CODE,
                    SystemConstants.USER_NOT_FOUND);
        }

        return new UserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        log.info("开始更新用户信息，ID: {}", id);

        // 1. 查找用户
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(SystemConstants.BUSINESS_ERROR_CODE,
                    SystemConstants.USER_NOT_FOUND);
        }

        // 2. 校验更新请求
        validateUpdateRequest(request, user);

        // 3. 构建更新条件
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);

        if (StringUtils.hasText(request.getRealName())) {
            updateWrapper.set("real_name", request.getRealName());
        }
        if (StringUtils.hasText(request.getPhoneNumber())) {
            updateWrapper.set("phone_number", request.getPhoneNumber());
        }
        if (StringUtils.hasText(request.getEmail())) {
            updateWrapper.set("email", request.getEmail());
        }
        if (request.getRemark() != null) {
            updateWrapper.set("remark", request.getRemark());
        }

        // 4. 执行更新
        userMapper.update(null, updateWrapper);

        // 5. 重新查询返回更新后的用户信息
        User updatedUser = userMapper.selectById(id);
        log.info("用户信息更新成功，ID: {}", updatedUser.getId());
        return new UserResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("开始删除用户，ID: {}", id);

        // 1. 检查用户是否存在
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(SystemConstants.BUSINESS_ERROR_CODE,
                    SystemConstants.USER_NOT_FOUND);
        }

        // 2. 执行删除 - 使用MP的deleteById
        userMapper.deleteById(id);

        log.info("用户删除成功，ID: {}", id);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        log.info("查询所有用户列表");

        // 使用MP的selectList方法
        List<User> users = userMapper.selectList(null);
        return users.stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> getUserPage(int page, int size) {
        log.info("分页查询用户列表，页码: {}, 每页大小: {}", page, size);

        // 参数校验
        if (page < 1) {
            throw new BusinessException(SystemConstants.BUSINESS_ERROR_CODE, "页码必须大于0");
        }
        if (size < 1 || size > 100) {
            throw new BusinessException(SystemConstants.BUSINESS_ERROR_CODE, "每页大小必须在1-100之间");
        }

        // 使用MyBatis-Plus分页插件
        Page<User> pageParam = new Page<>(page, size);
        Page<User> userPage = userMapper.selectPage(pageParam, null);

        return userPage.getRecords().stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> getUsersByType(String userType) {
        log.info("根据用户类型查询用户列表: {}", userType);

        // 校验用户类型
        if (!SystemConstants.USER_TYPE_ADMIN.equals(userType) &&
                !SystemConstants.USER_TYPE_FARMER.equals(userType)) {
            throw new BusinessException(SystemConstants.BUSINESS_ERROR_CODE, "无效的用户类型");
        }

        // 使用MP的QueryWrapper查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_type", userType);
        List<User> users = userMapper.selectList(queryWrapper);

        return users.stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public long getUserCount() {
        // 使用MP的selectCount方法
        return userMapper.selectCount(null);
    }

    @Override
    public boolean isUsernameExists(String username) {
        // 使用MP的QueryWrapper检查
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return userMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public boolean isPhoneExists(String phone) {
        // 使用MP的QueryWrapper检查
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone_number", phone);
        return userMapper.selectCount(queryWrapper) > 0;
    }



    // ========== 私有辅助方法 ==========

    /**
     * 校验用户创建请求
     */
    private void validateCreateRequest(UserCreateRequest request) {
        // 校验用户类型
        if (!SystemConstants.USER_TYPE_ADMIN.equals(request.getUserType()) &&
                !SystemConstants.USER_TYPE_FARMER.equals(request.getUserType())) {
            throw new BusinessException(SystemConstants.BUSINESS_ERROR_CODE,
                    "用户类型只能是 ADMIN 或 FARMER");
        }

        // 校验手机号格式（如果提供了手机号）
        if (StringUtils.hasText(request.getPhoneNumber()) &&
                !request.getPhoneNumber().matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException(SystemConstants.BUSINESS_ERROR_CODE, "手机号格式不正确");
        }
    }

    /**
     * 校验用户更新请求
     */
    private void validateUpdateRequest(UserUpdateRequest request, User existingUser) {
        // 如果要更新手机号，检查是否与其他用户冲突
        if (StringUtils.hasText(request.getPhoneNumber()) &&
                !request.getPhoneNumber().equals(existingUser.getPhoneNumber())) {
            QueryWrapper<User> phoneQuery = new QueryWrapper<>();
            phoneQuery.eq("phone_number", request.getPhoneNumber());
            if (userMapper.selectCount(phoneQuery) > 0) {
                throw new BusinessException(SystemConstants.BUSINESS_ERROR_CODE,
                        "手机号 '" + request.getPhoneNumber() + "' 已被其他用户使用");
            }
        }

        // 如果要更新邮箱，检查是否与其他用户冲突（只在邮箱不为空时检查）
        if (StringUtils.hasText(request.getEmail()) &&
                !request.getEmail().equals(existingUser.getEmail())) {
            QueryWrapper<User> emailQuery = new QueryWrapper<>();
            emailQuery.eq("email", request.getEmail());
            if (userMapper.selectCount(emailQuery) > 0) {
                throw new BusinessException(SystemConstants.BUSINESS_ERROR_CODE,
                        "邮箱 '" + request.getEmail() + "' 已被其他用户使用");
            }
        }
    }

    /**
     * 根据用户名或邮箱查找用户（用于登录）
     */
    public User findByUsernameOrEmail(String usernameOrEmail) {
        log.info("根据用户名或邮箱查询用户: {}", usernameOrEmail);

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                .eq("username", usernameOrEmail)
                .or()
                .eq("email", usernameOrEmail)
        );

        return userMapper.selectOne(queryWrapper);
    }
}