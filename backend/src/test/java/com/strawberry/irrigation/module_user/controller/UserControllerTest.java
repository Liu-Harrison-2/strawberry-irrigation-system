package com.strawberry.irrigation.module_user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strawberry.irrigation.common.exception.BusinessException;
import com.strawberry.irrigation.module_user.dto.UserCreateRequest;
import com.strawberry.irrigation.module_user.dto.UserResponse;
import com.strawberry.irrigation.module_user.dto.UserUpdateRequest;
import com.strawberry.irrigation.module_user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * 用户管理控制器测试类
 * 测试用户管理相关的REST API接口
 * 
 * 测试覆盖范围：
 * 1. 用户创建接口测试
 * 2. 用户查询接口测试 
 * 3. 用户更新接口测试
 * 4. 用户删除接口测试
 * 5. 参数校验测试
 * 6. 异常处理测试
 * 7. 边界条件测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("用户管理控制器测试")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserCreateRequest validCreateRequest;
    private UserUpdateRequest validUpdateRequest;
    private UserResponse mockUserResponse;

    @BeforeEach
    void setUp() {
        // 准备有效的用户创建请求数据
        validCreateRequest = new UserCreateRequest();
        validCreateRequest.setUsername("testuser");
        validCreateRequest.setPassword("123456");
        validCreateRequest.setRealName("测试用户");
        validCreateRequest.setPhoneNumber("13812345678");
        validCreateRequest.setUserType("FARMER");
        validCreateRequest.setEmail("test@example.com");
        validCreateRequest.setRemark("测试用户");

        // 准备有效的用户更新请求数据
        validUpdateRequest = new UserUpdateRequest();
        validUpdateRequest.setRealName("更新后的姓名");
        validUpdateRequest.setPhoneNumber("13999999999");
        validUpdateRequest.setEmail("updated@example.com");
        validUpdateRequest.setRemark("更新后的备注");

        // 准备模拟用户响应数据
        mockUserResponse = new UserResponse();
        mockUserResponse.setId(1L);
        mockUserResponse.setUsername("testuser");
        mockUserResponse.setRealName("测试用户");
        mockUserResponse.setPhone("13812345678");
        mockUserResponse.setUserType("FARMER");
        mockUserResponse.setStatus("INACTIVE");
        mockUserResponse.setCreatedAt(OffsetDateTime.now());
        mockUserResponse.setUpdatedAt(OffsetDateTime.now());
    }

    @Nested
    @DisplayName("用户创建接口测试")
    class CreateUserTests {

        @Test
        @DisplayName("正常创建用户 - 返回201状态码")
        void createUser_正常创建_返回201状态码() throws Exception {
            when(userService.createUser(any(UserCreateRequest.class))).thenReturn(mockUserResponse);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.username").value("testuser"))
                    .andExpect(jsonPath("$.data.realName").value("测试用户"))
                    .andExpect(jsonPath("$.data.phone").value("13812345678"))
                    .andExpect(jsonPath("$.data.userType").value("FARMER"));

            verify(userService, times(1)).createUser(any(UserCreateRequest.class));
        }

        @Test
        @DisplayName("用户名为空 - 返回400状态码")
        void createUser_用户名为空_返回400状态码() throws Exception {
            validCreateRequest.setUsername(""); // 设置为空

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userService, never()).createUser(any(UserCreateRequest.class));
        }

        @Test
        @DisplayName("用户名太短 - 返回400状态码")
        void createUser_用户名太短_返回400状态码() throws Exception {
            validCreateRequest.setUsername("ab"); // 小于3个字符

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userService, never()).createUser(any(UserCreateRequest.class));
        }

        @Test
        @DisplayName("用户名包含非法字符 - 返回400状态码")
        void createUser_用户名包含非法字符_返回400状态码() throws Exception {
            validCreateRequest.setUsername("test@user"); // 包含@符号

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userService, never()).createUser(any(UserCreateRequest.class));
        }

        @Test
        @DisplayName("密码太短 - 返回400状态码")
        void createUser_密码太短_返回400状态码() throws Exception {
            validCreateRequest.setPassword("12345"); // 小于6个字符

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userService, never()).createUser(any(UserCreateRequest.class));
        }

        @Test
        @DisplayName("手机号格式错误 - 返回400状态码")
        void createUser_手机号格式错误_返回400状态码() throws Exception {
            validCreateRequest.setPhoneNumber("12345678901"); // 不以1开头

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userService, never()).createUser(any(UserCreateRequest.class));
        }

        @Test
        @DisplayName("邮箱格式错误 - 返回400状态码")
        void createUser_邮箱格式错误_返回400状态码() throws Exception {
            validCreateRequest.setEmail("invalid-email"); // 邮箱格式错误

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userService, never()).createUser(any(UserCreateRequest.class));
        }

        @Test
        @DisplayName("用户名已存在 - 返回400状态码")
        void createUser_用户名已存在_返回400状态码() throws Exception {
            when(userService.createUser(any(UserCreateRequest.class)))
                    .thenThrow(new BusinessException(400, "用户名已存在"));

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userService, times(1)).createUser(any(UserCreateRequest.class));
        }
    }

    @Nested
    @DisplayName("用户查询接口测试")
    class GetUserTests {

        @Test
        @DisplayName("根据ID查询用户 - 成功返回用户信息")
        void getUserById_正常查询_返回用户信息() throws Exception {
            when(userService.getUserById(1L)).thenReturn(mockUserResponse);

            mockMvc.perform(get("/api/users/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.username").value("testuser"));

            verify(userService, times(1)).getUserById(1L);
        }

        @Test
        @DisplayName("根据ID查询不存在的用户 - 返回400状态码")
        void getUserById_用户不存在_返回400状态码() throws Exception {
            when(userService.getUserById(999L))
                    .thenThrow(new BusinessException(400, "用户不存在"));

            mockMvc.perform(get("/api/users/999"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userService, times(1)).getUserById(999L);
        }

        @Test
        @DisplayName("根据用户名查询用户 - 成功返回用户信息")
        void getUserByUsername_正常查询_返回用户信息() throws Exception {
            when(userService.getUserByUsername("testuser")).thenReturn(mockUserResponse);

            mockMvc.perform(get("/api/users/username/testuser"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.username").value("testuser"));

            verify(userService, times(1)).getUserByUsername("testuser");
        }

        @Test
        @DisplayName("根据空用户名查询 - 返回400状态码")
        void getUserByUsername_用户名为空_返回400状态码() throws Exception {
            mockMvc.perform(get("/api/users/username/"))
                    .andDo(print())
                    .andExpect(status().isNotFound()); // 404因为路径不匹配
        }

        @Test
        @DisplayName("获取所有用户列表 - 成功返回用户列表")
        void getAllUsers_正常查询_返回用户列表() throws Exception {
            List<UserResponse> userList = Arrays.asList(mockUserResponse);
            when(userService.getAllUsers()).thenReturn(userList);

            mockMvc.perform(get("/api/users"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].username").value("testuser"));

            verify(userService, times(1)).getAllUsers();
        }

        @Test
        @DisplayName("分页查询用户 - 成功返回分页数据")
        void getUserPage_正常分页_返回分页数据() throws Exception {
            List<UserResponse> userList = Arrays.asList(mockUserResponse);
            when(userService.getUserPage(1, 10)).thenReturn(userList);

            mockMvc.perform(get("/api/users/page?page=1&size=10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").isArray());

            verify(userService, times(1)).getUserPage(1, 10);
        }

        @Test
        @DisplayName("分页查询用户传入无效参数 - 返回400状态码")
        void getUserPage_无效参数_返回400状态码() throws Exception {
            mockMvc.perform(get("/api/users/page?page=0&size=10")) // page不能为0
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userService, never()).getUserPage(anyInt(), anyInt());
        }

        @Test
        @DisplayName("根据用户类型查询 - 成功返回用户列表")
        void getUsersByType_正常查询_返回用户列表() throws Exception {
            List<UserResponse> farmerList = Arrays.asList(mockUserResponse);
            when(userService.getUsersByType("FARMER")).thenReturn(farmerList);

            mockMvc.perform(get("/api/users/type/FARMER"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").isArray());

            verify(userService, times(1)).getUsersByType("FARMER");
        }

        @Test
        @DisplayName("获取用户总数 - 成功返回数量")
        void getUserCount_正常查询_返回数量() throws Exception {
            when(userService.getUserCount()).thenReturn(10L);

            mockMvc.perform(get("/api/users/count"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").value(10));

            verify(userService, times(1)).getUserCount();
        }

        @Test
        @DisplayName("检查用户名是否存在 - 返回检查结果")
        void checkUsernameExists_用户名存在_返回true() throws Exception {
            when(userService.isUsernameExists("testuser")).thenReturn(true);

            mockMvc.perform(get("/api/users/check/username/testuser"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").value(true));

            verify(userService, times(1)).isUsernameExists("testuser");
        }

        @Test
        @DisplayName("检查手机号是否存在 - 返回检查结果")
        void checkPhoneExists_手机号存在_返回true() throws Exception {
            when(userService.isPhoneExists("13812345678")).thenReturn(true);

            mockMvc.perform(get("/api/users/check/phone/13812345678"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").value(true));

            verify(userService, times(1)).isPhoneExists("13812345678");
        }
    }

    @Nested
    @DisplayName("用户更新接口测试")
    class UpdateUserTests {

        @Test
        @DisplayName("正常更新用户 - 返回200状态码")
        void updateUser_正常更新_返回200状态码() throws Exception {
            UserResponse updatedResponse = new UserResponse();
            updatedResponse.setId(mockUserResponse.getId());
            updatedResponse.setUsername(mockUserResponse.getUsername());
            updatedResponse.setRealName("更新后的姓名");
            updatedResponse.setPhone("更新后的手机号");
            
            when(userService.updateUser(eq(1L), any(UserUpdateRequest.class)))
                    .thenReturn(updatedResponse);

            mockMvc.perform(put("/api/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.realName").value("更新后的姓名"));

            verify(userService, times(1)).updateUser(eq(1L), any(UserUpdateRequest.class));
        }

        @Test
        @DisplayName("更新不存在的用户 - 返回400状态码")
        void updateUser_用户不存在_返回400状态码() throws Exception {
            when(userService.updateUser(eq(999L), any(UserUpdateRequest.class)))
                    .thenThrow(new BusinessException(400, "用户不存在"));

            mockMvc.perform(put("/api/users/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userService, times(1)).updateUser(eq(999L), any(UserUpdateRequest.class));
        }

        @Test
        @DisplayName("更新用户邮箱格式错误 - 返回400状态码")
        void updateUser_邮箱格式错误_返回400状态码() throws Exception {
            validUpdateRequest.setEmail("invalid-email");

            mockMvc.perform(put("/api/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userService, never()).updateUser(anyLong(), any(UserUpdateRequest.class));
        }

        @Test
        @DisplayName("更新用户手机号格式错误 - 返回400状态码")
        void updateUser_手机号格式错误_返回400状态码() throws Exception {
            validUpdateRequest.setPhoneNumber("12345678901"); // 不以1开头

            mockMvc.perform(put("/api/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userService, never()).updateUser(anyLong(), any(UserUpdateRequest.class));
        }

        @Test
        @DisplayName("更新用户姓名超出长度限制 - 返回400状态码")
        void updateUser_姓名超出长度_返回400状态码() throws Exception {
            validUpdateRequest.setRealName("a".repeat(51)); // 超过50个字符

            mockMvc.perform(put("/api/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userService, never()).updateUser(anyLong(), any(UserUpdateRequest.class));
        }
    }

    @Nested
    @DisplayName("用户删除接口测试")
    class DeleteUserTests {

        @Test
        @DisplayName("正常删除用户 - 返回200状态码")
        void deleteUser_正常删除_返回200状态码() throws Exception {
            doNothing().when(userService).deleteUser(1L);

            mockMvc.perform(delete("/api/users/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));

            verify(userService, times(1)).deleteUser(1L);
        }

        @Test
        @DisplayName("删除不存在的用户 - 返回400状态码")
        void deleteUser_用户不存在_返回400状态码() throws Exception {
            doThrow(new BusinessException(400, "用户不存在"))
                    .when(userService).deleteUser(999L);

            mockMvc.perform(delete("/api/users/999"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userService, times(1)).deleteUser(999L);
        }
    }

    @Nested
    @DisplayName("健康检查接口测试")
    class HealthCheckTests {

        @Test
        @DisplayName("健康检查 - 返回服务状态")
        void healthCheck_API调用_返回服务状态() throws Exception {
            mockMvc.perform(get("/api/system/health"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("UP"));
        }
    }
}