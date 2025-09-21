package com.strawberry.irrigation.module_user.controller;

import com.strawberry.irrigation.common.constants.SystemConstants;
import com.strawberry.irrigation.common.response.Result;
import com.strawberry.irrigation.module_user.dto.UserCreateRequest;
import com.strawberry.irrigation.module_user.dto.UserResponse;
import com.strawberry.irrigation.module_user.dto.UserUpdateRequest;
import com.strawberry.irrigation.module_user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * 用户管理控制器
 * 提供用户相关的REST API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    /**
     * 创建新用户
     * POST /api/users
     */
    @PostMapping
    public ResponseEntity<Result<UserResponse>> createUser(@Valid @RequestBody UserCreateRequest request) {
        log.info("接收到创建用户请求，用户名: {}", request.getUsername());

        UserResponse userResponse = userService.createUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Result.success(SystemConstants.SUCCESS_CODE,
                        SystemConstants.USER_CREATE_SUCCESS, userResponse));
    }

    /**
     * 根据ID获取用户信息
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result<UserResponse>> getUserById(@PathVariable Long id) {
        log.info("接收到查询用户请求，ID: {}", id);

        UserResponse userResponse = userService.getUserById(id);

        return ResponseEntity.ok(Result.success(userResponse));
    }

    /**
     * 根据用户名获取用户信息
     * GET /api/users/username/{username}
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<Result<UserResponse>> getUserByUsername(
            @PathVariable @NotBlank(message = "用户名不能为空") String username) {
        log.info("接收到根据用户名查询用户请求，用户名: {}", username);

        UserResponse userResponse = userService.getUserByUsername(username);

        return ResponseEntity.ok(Result.success(userResponse));
    }

    /**
     * 更新用户信息
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Result<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("接收到更新用户请求，ID: {}", id);

        UserResponse userResponse = userService.updateUser(id, request);

        return ResponseEntity.ok(Result.success(SystemConstants.SUCCESS_CODE,
                SystemConstants.USER_UPDATE_SUCCESS, userResponse));
    }

    /**
     * 删除用户
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteUser(@PathVariable Long id) {
        log.info("接收到删除用户请求，ID: {}", id);

        userService.deleteUser(id);

        return ResponseEntity.ok(Result.success(SystemConstants.SUCCESS_CODE,
                SystemConstants.USER_DELETE_SUCCESS, null));
    }

    /**
     * 获取所有用户列表
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<Result<List<UserResponse>>> getAllUsers() {
        log.info("接收到查询所有用户请求");

        List<UserResponse> users = userService.getAllUsers();

        return ResponseEntity.ok(Result.success(users));
    }

    /**
     * 分页获取用户列表
     * GET /api/users/page?page=1&size=10
     */
    @GetMapping("/page")
    public ResponseEntity<Result<List<UserResponse>>> getUserPage(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") int size) {
        log.info("接收到分页查询用户请求，页码: {}, 每页大小: {}", page, size);

        List<UserResponse> users = userService.getUserPage(page, size);

        return ResponseEntity.ok(Result.success(users));
    }

    /**
     * 根据用户类型获取用户列表
     * GET /api/users/type/{userType}
     */
    @GetMapping("/type/{userType}")
    public ResponseEntity<Result<List<UserResponse>>> getUsersByType(@PathVariable String userType) {
        log.info("接收到根据用户类型查询用户请求，类型: {}", userType);

        List<UserResponse> users = userService.getUsersByType(userType);

        return ResponseEntity.ok(Result.success(users));
    }

    /**
     * 获取用户总数
     * GET /api/users/count
     */
    @GetMapping("/count")
    public ResponseEntity<Result<Long>> getUserCount() {
        log.info("接收到查询用户总数请求");

        long count = userService.getUserCount();

        return ResponseEntity.ok(Result.success(count));
    }

    /**
     * 检查用户名是否存在
     * GET /api/users/check/username/{username}
     */
    @GetMapping("/check/username/{username}")
    public ResponseEntity<Result<Boolean>> checkUsernameExists(@PathVariable String username) {
        log.info("接收到检查用户名是否存在请求，用户名: {}", username);

        boolean exists = userService.isUsernameExists(username);

        return ResponseEntity.ok(Result.success(exists));
    }

    /**
     * 检查手机号是否存在
     * GET /api/users/check/phone/{phone}
     */
    @GetMapping("/check/phone/{phone}")
    public ResponseEntity<Result<Boolean>> checkPhoneExists(@PathVariable String phone) {
        log.info("接收到检查手机号是否存在请求，手机号: {}", phone);

        boolean exists = userService.isPhoneExists(phone);

        return ResponseEntity.ok(Result.success(exists));
    }
}