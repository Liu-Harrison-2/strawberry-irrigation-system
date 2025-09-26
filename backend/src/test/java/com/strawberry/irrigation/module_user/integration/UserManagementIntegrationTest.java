package com.strawberry.irrigation.module_user.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strawberry.irrigation.module_user.dto.UserCreateRequest;
import com.strawberry.irrigation.module_user.dto.UserResponse;
import com.strawberry.irrigation.module_user.dto.UserUpdateRequest;
import com.strawberry.irrigation.module_user.entity.User;
import com.strawberry.irrigation.module_user.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 用户管理模块集成测试
 * 测试完整的用户管理流程，包括创建、查询、更新、删除用户的完整业务流程
 * 
 * 测试覆盖范围：
 * 1. 完整的用户生命周期管理
 * 2. API接口与业务逻辑的集成
 * 3. 数据库事务处理
 * 4. 异常场景的端到端测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.yml")
@DisplayName("用户管理模块集成测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    private static UserCreateRequest testUserCreateRequest;
    private static Long createdUserId;

    @BeforeAll
    static void setUpClass() {
        // 准备测试用户数据
        testUserCreateRequest = new UserCreateRequest();
        testUserCreateRequest.setUsername("integrationtest");
        testUserCreateRequest.setPassword("123456");
        testUserCreateRequest.setRealName("集成测试用户");
        testUserCreateRequest.setPhoneNumber("13812345678");
        testUserCreateRequest.setUserType("FARMER");
        testUserCreateRequest.setEmail("integration@test.com");
        testUserCreateRequest.setRemark("集成测试用户备注");
    }

    @Test
    @Order(1)
    @DisplayName("完整用户管理流程测试 - 创建用户")
    void integrationTest_01_createUser() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserCreateRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("用户创建成功"))
                .andExpect(jsonPath("$.data.username").value("integrationtest"))
                .andExpect(jsonPath("$.data.realName").value("集成测试用户"))
                .andExpect(jsonPath("$.data.phone").value("13812345678"))
                .andExpect(jsonPath("$.data.userType").value("FARMER"))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"))
                .andExpect(jsonPath("$.data.id").exists())
                .andDo(result -> {
                    // 提取创建的用户ID，供后续测试使用
                    String responseContent = result.getResponse().getContentAsString();
                    UserResponse userResponse = objectMapper.readTree(responseContent)
                            .get("data").traverse(objectMapper).readValueAs(UserResponse.class);
                    createdUserId = userResponse.getId();
                });
    }

    @Test
    @Order(2)
    @DisplayName("完整用户管理流程测试 - 查询用户")
    void integrationTest_02_getUserById() throws Exception {
        mockMvc.perform(get("/api/users/{id}", createdUserId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(createdUserId))
                .andExpect(jsonPath("$.data.username").value("integrationtest"))
                .andExpect(jsonPath("$.data.realName").value("集成测试用户"));
    }

    @Test
    @Order(3)
    @DisplayName("完整用户管理流程测试 - 根据用户名查询用户")
    void integrationTest_03_getUserByUsername() throws Exception {
        mockMvc.perform(get("/api/users/username/{username}", "integrationtest"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value("integrationtest"))
                .andExpect(jsonPath("$.data.id").value(createdUserId));
    }

    @Test
    @Order(4)
    @DisplayName("完整用户管理流程测试 - 检查用户名存在")
    void integrationTest_04_checkUsernameExists() throws Exception {
        mockMvc.perform(get("/api/users/check/username/{username}", "integrationtest"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));

        // 检查不存在的用户名
        mockMvc.perform(get("/api/users/check/username/{username}", "nonexistent"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @Order(5)
    @DisplayName("完整用户管理流程测试 - 检查手机号存在")
    void integrationTest_05_checkPhoneExists() throws Exception {
        mockMvc.perform(get("/api/users/check/phone/{phone}", "13812345678"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @Order(6)
    @DisplayName("完整用户管理流程测试 - 获取用户列表")
    void integrationTest_06_getAllUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[?(@.username == 'integrationtest')]").exists());
    }

    @Test
    @Order(7)
    @DisplayName("完整用户管理流程测试 - 分页查询用户")
    void integrationTest_07_getUserPage() throws Exception {
        mockMvc.perform(get("/api/users/page")
                        .param("page", "1")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @Order(8)
    @DisplayName("完整用户管理流程测试 - 根据类型查询用户")
    void integrationTest_08_getUsersByType() throws Exception {
        mockMvc.perform(get("/api/users/type/{userType}", "FARMER"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[?(@.username == 'integrationtest')]").exists());
    }

    @Test
    @Order(9)
    @DisplayName("完整用户管理流程测试 - 获取用户总数")
    void integrationTest_09_getUserCount() throws Exception {
        mockMvc.perform(get("/api/users/count"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(greaterThanOrEqualTo(1)));
    }

    @Test
    @Order(10)
    @DisplayName("完整用户管理流程测试 - 更新用户")
    void integrationTest_10_updateUser() throws Exception {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setRealName("更新后的集成测试用户");
        updateRequest.setPhoneNumber("13999999999");
        updateRequest.setEmail("updated@integration.com");
        updateRequest.setRemark("更新后的备注");

        mockMvc.perform(put("/api/users/{id}", createdUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("用户信息更新成功"))
                .andExpect(jsonPath("$.data.realName").value("更新后的集成测试用户"))
                .andExpect(jsonPath("$.data.phone").value("13999999999"))
                .andExpect(jsonPath("$.data.email").value("updated@integration.com"));
    }

    @Test
    @Order(11)
    @DisplayName("完整用户管理流程测试 - 验证更新后的用户信息")
    void integrationTest_11_verifyUpdatedUser() throws Exception {
        mockMvc.perform(get("/api/users/{id}", createdUserId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.realName").value("更新后的集成测试用户"))
                .andExpect(jsonPath("$.data.phone").value("13999999999"))
                .andExpect(jsonPath("$.data.email").value("updated@integration.com"));
    }

    @Test
    @Order(12)
    @DisplayName("完整用户管理流程测试 - 删除用户")
    void integrationTest_12_deleteUser() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", createdUserId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("用户删除成功"));
    }

    @Test
    @Order(13)
    @DisplayName("完整用户管理流程测试 - 验证用户已删除")
    void integrationTest_13_verifyUserDeleted() throws Exception {
        mockMvc.perform(get("/api/users/{id}", createdUserId))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Nested
    @DisplayName("异常场景集成测试")
    class ExceptionScenarioTests {

        @Test
        @DisplayName("重复创建相同用户名 - 返回业务异常")
        void duplicateUsername_shouldReturnBusinessException() throws Exception {
            // 先创建一个用户
            UserCreateRequest firstUser = new UserCreateRequest();
            firstUser.setUsername("duplicatetest");
            firstUser.setPassword("123456");
            firstUser.setRealName("重复测试用户1");
            firstUser.setPhoneNumber("13800000001");
            firstUser.setUserType("FARMER");
            firstUser.setEmail("duplicate1@test.com");

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(firstUser)))
                    .andExpect(status().isCreated());

            // 尝试创建相同用户名的用户
            UserCreateRequest secondUser = new UserCreateRequest();
            secondUser.setUsername("duplicatetest"); // 相同用户名
            secondUser.setPassword("123456");
            secondUser.setRealName("重复测试用户2");
            secondUser.setPhoneNumber("13800000002");
            secondUser.setUserType("FARMER");
            secondUser.setEmail("duplicate2@test.com");

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(secondUser)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("重复手机号 - 返回业务异常")
        void duplicatePhone_shouldReturnBusinessException() throws Exception {
            // 先创建一个用户
            UserCreateRequest firstUser = new UserCreateRequest();
            firstUser.setUsername("phonetest1");
            firstUser.setPassword("123456");
            firstUser.setRealName("手机号测试用户1");
            firstUser.setPhoneNumber("13700000001");
            firstUser.setUserType("FARMER");
            firstUser.setEmail("phone1@test.com");

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(firstUser)))
                    .andExpect(status().isCreated());

            // 尝试创建相同手机号的用户
            UserCreateRequest secondUser = new UserCreateRequest();
            secondUser.setUsername("phonetest2");
            secondUser.setPassword("123456");
            secondUser.setRealName("手机号测试用户2");
            secondUser.setPhoneNumber("13700000001"); // 相同手机号
            secondUser.setUserType("FARMER");
            secondUser.setEmail("phone2@test.com");

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(secondUser)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }
    }

    @Nested
    @DisplayName("健康检查测试")
    class HealthCheckTests {

        @Test
        @DisplayName("健康检查接口 - 返回服务状态")
        void healthCheck_shouldReturnHealthStatus() throws Exception {
            mockMvc.perform(get("/api/system/health"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("UP"));
        }
    }
}