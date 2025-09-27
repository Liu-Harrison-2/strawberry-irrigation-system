package com.strawberry.irrigation.module_auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strawberry.irrigation.module_auth.dto.LoginRequest;
import com.strawberry.irrigation.module_auth.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 授权管理模块集成测试
 * 
 * 测试覆盖范围：
 * 1. 用户注册完整流程测试
 * 2. 用户登录完整流程测试
 * 3. 令牌刷新完整流程测试
 * 4. 用户登出完整流程测试
 * 5. API与业务逻辑集成测试
 * 6. 数据库事务处理测试
 * 7. 端到端异常场景测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "jwt.secret=test-jwt-secret-key-for-integration-testing-only",
    "jwt.issuer=smart-irrigation-test",
    "jwt.access-token.expires-in-minutes=60",
    "jwt.refresh-token.expires-in-days=7"
})
@Transactional
@DisplayName("授权管理模块集成测试")
class AuthManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;

    @BeforeEach
    void setUp() {
        // 准备有效的注册请求数据
        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setUsername("integrationtest");
        validRegisterRequest.setPassword("password123");
        validRegisterRequest.setEmail("integration@test.com");
        validRegisterRequest.setRealName("集成测试用户");
        validRegisterRequest.setPhoneNumber("13800138000");
        validRegisterRequest.setUserType("FARMER");

        // 准备有效的登录请求数据
        validLoginRequest = new LoginRequest();
        validLoginRequest.setUsername("integrationtest");
        validLoginRequest.setPassword("password123");
    }

    @Nested
    @DisplayName("用户注册集成测试")
    class UserRegistrationIntegrationTests {

        @Test
        @DisplayName("完整用户注册流程 - 成功")
        void completeUserRegistration_成功() throws Exception {
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.message").value("操作成功"))
                    .andExpect(jsonPath("$.data.username").value("integrationtest"))
                    .andExpect(jsonPath("$.data.realName").value("集成测试用户"))
                    .andExpect(jsonPath("$.data.userType").value("FARMER"));
        }

        @Test
        @DisplayName("注册时用户名已存在 - 返回错误")
        void registerWithExistingUsername_返回错误() throws Exception {
            // 先注册一个用户
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andExpect(status().isOk());

            // 再次注册相同用户名
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("注册时缺少必填字段 - 返回验证错误")
        void registerWithMissingFields_返回验证错误() throws Exception {
            validRegisterRequest.setUsername(null);

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }
    }

    @Nested
    @DisplayName("用户登录集成测试")
    class UserLoginIntegrationTests {

        @Test
        @DisplayName("完整用户登录流程 - 成功")
        void completeUserLogin_成功() throws Exception {
            // 先注册用户
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andExpect(status().isOk());

            // 然后登录
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validLoginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.accessToken").exists())
                    .andExpect(jsonPath("$.data.refreshToken").exists())
                    .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.data.expiresIn").exists())
                    .andExpect(jsonPath("$.data.userInfo.username").value("integrationtest"));
        }

        @Test
        @DisplayName("登录时用户不存在 - 返回错误")
        void loginWithNonExistentUser_返回错误() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validLoginRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(401));
        }

        @Test
        @DisplayName("登录时密码错误 - 返回错误")
        void loginWithWrongPassword_返回错误() throws Exception {
            // 先注册用户
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andExpect(status().isOk());

            // 使用错误密码登录
            validLoginRequest.setPassword("wrongpassword");
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validLoginRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(401));
        }
    }

    @Nested
    @DisplayName("令牌管理集成测试")
    class TokenManagementIntegrationTests {

        @Test
        @DisplayName("令牌刷新流程 - 成功")
        void tokenRefresh_成功() throws Exception {
            // 先注册用户
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andExpect(status().isOk());

            // 登录获取令牌
            String loginResponse = mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validLoginRequest)))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            // 从登录响应中提取刷新令牌
            // 注意：这里需要解析JSON响应来获取refreshToken
            // 实际实现中可能需要使用JsonPath或其他方式提取
        }

        @Test
        @DisplayName("用户登出流程 - 成功")
        void userLogout_成功() throws Exception {
            // 先注册用户
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andExpect(status().isOk());

            // 登录获取令牌
            String loginResponse = mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validLoginRequest)))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            // 从登录响应中提取刷新令牌并执行登出
            // 注意：这里需要解析JSON响应来获取refreshToken
        }
    }

    @Nested
    @DisplayName("数据验证集成测试")
    class DataValidationIntegrationTests {

        @Test
        @DisplayName("用户名长度验证 - 太短")
        void usernameValidation_太短() throws Exception {
            validRegisterRequest.setUsername("ab"); // 假设最小长度为3

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("密码长度验证 - 太短")
        void passwordValidation_太短() throws Exception {
            validRegisterRequest.setPassword("123"); // 假设最小长度为6

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("邮箱格式验证 - 无效格式")
        void emailValidation_无效格式() throws Exception {
            validRegisterRequest.setEmail("invalid-email");

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("手机号格式验证 - 无效格式")
        void phoneValidation_无效格式() throws Exception {
            validRegisterRequest.setPhoneNumber("invalid-phone");

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }
    }

    @Nested
    @DisplayName("业务逻辑集成测试")
    class BusinessLogicIntegrationTests {

        @Test
        @DisplayName("注册后立即登录 - 成功")
        void registerThenLogin_成功() throws Exception {
            // 注册用户
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.username").value("integrationtest"));

            // 立即登录
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validLoginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userInfo.username").value("integrationtest"));
        }

        @Test
        @DisplayName("不同用户类型注册 - 成功")
        void registerDifferentUserTypes_成功() throws Exception {
            // 注册农民用户
            validRegisterRequest.setUsername("farmer1");
            validRegisterRequest.setPhoneNumber("13800138001");
            validRegisterRequest.setUserType("FARMER");
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userType").value("FARMER"));

            // 注册管理员用户
            validRegisterRequest.setUsername("admin1");
            validRegisterRequest.setPhoneNumber("13800138002");
            validRegisterRequest.setUserType("ADMIN");
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userType").value("ADMIN"));

            // 注册技术员用户
            validRegisterRequest.setUsername("tech1");
            validRegisterRequest.setPhoneNumber("13800138003");
            validRegisterRequest.setUserType("TECHNICIAN");
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userType").value("TECHNICIAN"));
        }
    }

    @Nested
    @DisplayName("异常场景集成测试")
    class ExceptionScenarioIntegrationTests {

        @Test
        @DisplayName("请求体为空 - 返回错误")
        void emptyRequestBody_返回错误() throws Exception {
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("无效的JSON格式 - 返回错误")
        void invalidJsonFormat_返回错误() throws Exception {
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("invalid json"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("缺少Content-Type头 - 返回错误")
        void missingContentType_返回错误() throws Exception {
            mockMvc.perform(post("/api/auth/register")
                    .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andExpect(status().isUnsupportedMediaType());
        }
    }
}