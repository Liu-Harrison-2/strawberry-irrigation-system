package com.strawberry.irrigation.module_user.service.impl;

import com.strawberry.irrigation.common.constants.SystemConstants;
import com.strawberry.irrigation.common.exception.BusinessException;
import com.strawberry.irrigation.module_user.dto.UserCreateRequest;
import com.strawberry.irrigation.module_user.dto.UserResponse;
import com.strawberry.irrigation.module_user.dto.UserUpdateRequest;
import com.strawberry.irrigation.module_user.entity.User;
import com.strawberry.irrigation.module_user.repository.UserRepository;
import com.strawberry.irrigation.module_user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 * 处理用户管理的核心业务逻辑
 */
@Slf4j //添加输出日志
@Service 
@RequiredArgsConstructor //自动生成构造函数
@Transactional(readOnly = true) //只读事务
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional // 方法级别：覆盖类级别设置，使用读写事务支持数据修改
    public UserResponse createUser(UserCreateRequest request) {
        log.info("开始创建用户，用户名: {}", request.getUsername());

        // 1. 校验用户类型和手机号手机号
        validateCreateRequest(request);

        // 2. 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(SystemConstants.BUSINESS_ERROR_CODE,
                    "用户名 '" + request.getUsername() + "' 已存在");
        }

        // 3. 检查手机号是否已存在
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException(SystemConstants.BUSINESS_ERROR_CODE,
                    "手机号 '" + request.getPhone() + "' 已存在");
        }

        // 4. 创建用户实体
        User user = new User(
                request.getUsername(),
                request.getRealName(),
                request.getPhone(),
                request.getEmail(),
                request.getUserType()
        );
        user.setRemark(request.getRemark());

        // 5. 保存用户
        User savedUser = userRepository.save(user);

        log.info("用户创建成功，ID: {}, 用户名: {}", savedUser.getId(), savedUser.getUsername());
        return new UserResponse(savedUser);
    }

    @Override
    public UserResponse getUserById(Long id) {
        log.info("根据ID查询用户: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(SystemConstants.BUSINESS_ERROR_CODE,
                        SystemConstants.USER_NOT_FOUND));

        return new UserResponse(user);
    }

    @Override
    public UserResponse getUserByUsername(String username) {
        log.info("根据用户名查询用户: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(SystemConstants.BUSINESS_ERROR_CODE,
                        SystemConstants.USER_NOT_FOUND));

        return new UserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        log.info("开始更新用户信息，ID: {}", id);

        // 1. 查找用户
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(SystemConstants.BUSINESS_ERROR_CODE,
                        SystemConstants.USER_NOT_FOUND));

        // 2. 校验更新请求
        validateUpdateRequest(request, user);

        // 3. 更新用户信息
        updateUserFields(user, request);
        user.setUpdateTime(LocalDateTime.now());

        // 4. 保存更新
        User updatedUser = userRepository.save(user);

        log.info("用户信息更新成功，ID: {}", updatedUser.getId());
        return new UserResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("开始删除用户，ID: {}", id);

        // 1. 检查用户是否存在
        if (!userRepository.existsById(id)) {
            throw new BusinessException(SystemConstants.BUSINESS_ERROR_CODE,
                    SystemConstants.USER_NOT_FOUND);
        }

        // 2. 执行删除
        userRepository.deleteById(id);

        log.info("用户删除成功，ID: {}", id);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        log.info("查询所有用户列表");

        List<User> users = userRepository.findAll();
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

        List<User> users = userRepository.findPage(page, size);
        return users.stream()
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

        List<User> users = userRepository.findAll().stream()
                .filter(user -> userType.equals(user.getUserType()))
                .collect(Collectors.toList());

        return users.stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public long getUserCount() {
        return userRepository.count();
    }

    @Override
    public boolean isUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean isPhoneExists(String phone) {
        return userRepository.existsByPhone(phone);
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

        // 校验手机号格式（这里可以添加更多业务规则）
        if (!request.getPhone().matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException(SystemConstants.BUSINESS_ERROR_CODE, "手机号格式不正确");
        }
    }

    /**
     * 校验用户更新请求
     */
    private void validateUpdateRequest(UserUpdateRequest request, User existingUser) {
        // 如果要更新手机号，检查是否与其他用户冲突
        if (StringUtils.hasText(request.getPhone()) && //不为空且不为空字符串
                !request.getPhone().equals(existingUser.getPhone())) {  // 验证原来的手机号是否与更新的不同
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new BusinessException(SystemConstants.BUSINESS_ERROR_CODE,
                        "手机号 '" + request.getPhone() + "' 已被其他用户使用");
            }
        }
    }

    /**
     * 更新用户字段
     */
    private void updateUserFields(User user, UserUpdateRequest request) {
        if (StringUtils.hasText(request.getRealName())) {
            user.setRealName(request.getRealName());
        }
        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        if (StringUtils.hasText(request.getEmail())) {
            user.setEmail(request.getEmail());
        }
        if (request.getRemark() != null) {
            user.setRemark(request.getRemark());
        }
    }
}