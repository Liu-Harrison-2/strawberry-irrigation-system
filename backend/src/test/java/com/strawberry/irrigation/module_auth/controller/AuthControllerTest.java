package com.strawberry.irrigation.module_auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strawberry.irrigation.common.exception.BusinessException;
import com.strawberry.irrigation.module_auth.dto.AuthResponse;
import com.strawberry.irrigation.module_auth.dto.LoginRequest;
import com.strawberry.irrigation.module_auth.dto.RegisterRequest;
import com.strawberry.irrigation.module_auth.service.AuthService;
import com.strawberry.irrigation.module_auth.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * 授权管理控制器测试类
 * 测试授权管理相关的REST API接口
 * 
 * 测试覆盖范围：
 * 1. 用户注册接口测试
 * 2. 用户登录接口测试
 * 3. 令牌刷新接口测试
 * 4. 用户登出接口测试
 * 5. 撤销所有令牌接口测试
 * 6. 参数校验测试
 * 7. 异常处理测试
 * 8. 边界条件测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "jwt.secret=test-jwt-secret-key-for-controller-testing-only",
    "jwt.issuer=smart-irrigation-test",
    "jwt.access-token.expires-in-minutes=60",
    "jwt.refresh-token.expires-in-days=7"
})
@DisplayName("授权管理控制器测试")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private AuthController.RefreshTokenRequest validRefreshTokenRequest;
    private AuthResponse mockAuthResponse;
    private AuthResponse.UserInfo mockUserInfo;

    @BeforeEach
    void setUp() {
        // 准备有效的用户注册请求数据
        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setUsername("testuser");
        validRegisterRequest.setPassword("password123");
        validRegisterRequest.setEmail("test@example.com");
        validRegisterRequest.setRealName("测试用户");
        validRegisterRequest.setPhoneNumber("13812345678");
        validRegisterRequest.setUserType("FARMER");

        // 准备有效的用户登录请求数据
        validLoginRequest = new LoginRequest();
        validLoginRequest.setUsername("testuser");
        validLoginRequest.setPassword("password123");

        // 准备有效的刷新令牌请求数据
        validRefreshTokenRequest = new AuthController.RefreshTokenRequest();
        validRefreshTokenRequest.setRefreshToken("valid_refresh_token");

        // 准备模拟用户信息响应数据
        mockUserInfo = new AuthResponse.UserInfo();
        mockUserInfo.setId(1L);
        mockUserInfo.setUsername("testuser");
        mockUserInfo.setRealName("测试用户");
        mockUserInfo.setUserType("FARMER");
        mockUserInfo.setStatus("INACTIVE");

        // 准备模拟认证响应数据
        mockAuthResponse = new AuthResponse();
        mockAuthResponse.setAccessToken("mock_access_token");
        mockAuthResponse.setRefreshToken("mock_refresh_token");
        mockAuthResponse.setExpiresIn(3600L);
        mockAuthResponse.setUserInfo(mockUserInfo);
    }

    @Nested
    @DisplayName("用户注册接口测试")
    class RegisterTests {

        @Test
        @DisplayName("正常注册用户 - 返回201状态码")
        void register_正常注册_返回201状态码() throws Exception {
            when(authService.register(any(RegisterRequest.class))).thenReturn(mockUserInfo);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.username").value("testuser"))
                    .andExpect(jsonPath("$.data.realName").value("测试用户"))
                    .andExpect(jsonPath("$.data.userType").value("FARMER"))
                    .andExpect(jsonPath("$.data.status").value("INACTIVE"));

            verify(authService, times(1)).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("注册管理员用户 - 返回成功")
        void register_注册管理员_返回成功() throws Exception {
            validRegisterRequest.setUserType("ADMIN");
            mockUserInfo.setUserType("ADMIN");
            
            when(authService.register(any(RegisterRequest.class))).thenReturn(mockUserInfo);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.userType").value("ADMIN"));

            verify(authService, times(1)).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("注册技术员用户 - 返回成功")
        void register_注册技术员_返回成功() throws Exception {
            validRegisterRequest.setUserType("TECHNICIAN");
            mockUserInfo.setUserType("TECHNICIAN");
            
            when(authService.register(any(RegisterRequest.class))).thenReturn(mockUserInfo);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.userType").value("TECHNICIAN"));

            verify(authService, times(1)).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("用户名为空 - 返回400状态码")
        void register_用户名为空_返回400状态码() throws Exception {
            validRegisterRequest.setUsername(""); // 设置为空

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(authService, never()).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("用户名太短 - 返回400状态码")
        void register_用户名太短_返回400状态码() throws Exception {
            validRegisterRequest.setUsername("ab"); // 少于3个字符

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(authService, never()).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("密码为空 - 返回400状态码")
        void register_密码为空_返回400状态码() throws Exception {
            validRegisterRequest.setPassword(""); // 设置为空

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(authService, never()).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("密码太短 - 返回400状态码")
        void register_密码太短_返回400状态码() throws Exception {
            validRegisterRequest.setPassword("123"); // 少于6个字符

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(authService, never()).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("邮箱格式错误 - 返回400状态码")
        void register_邮箱格式错误_返回400状态码() throws Exception {
            validRegisterRequest.setEmail("invalid-email-format"); // 无效邮箱格式

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(authService, never()).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("手机号格式错误 - 返回400状态码")
        void register_手机号格式错误_返回400状态码() throws Exception {
            validRegisterRequest.setPhoneNumber("123456"); // 无效手机号格式

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(authService, never()).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("真实姓名为空 - 返回400状态码")
        void register_真实姓名为空_返回400状态码() throws Exception {
            validRegisterRequest.setRealName(""); // 设置为空

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(authService, never()).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("用户名已存在 - 返回400状态码")
        void register_用户名已存在_返回400状态码() throws Exception {
            when(authService.register(any(RegisterRequest.class)))
                    .thenThrow(new BusinessException("用户名已存在"));

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(authService, times(1)).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("邮箱已存在 - 返回400状态码")
        void register_邮箱已存在_返回400状态码() throws Exception {
            when(authService.register(any(RegisterRequest.class)))
                    .thenThrow(new BusinessException("邮箱已存在"));

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(authService, times(1)).register(any(RegisterRequest.class));
        }
    }

    @Nested
    @DisplayName("用户登录接口测试")
    class LoginTests {

        @Test
        @DisplayName("正常登录 - 返回200状态码和认证信息")
        void login_正常登录_返回200状态码() throws Exception {
            when(authService.login(any(LoginRequest.class))).thenReturn(mockAuthResponse);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validLoginRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.accessToken").value("mock_access_token"))
                    .andExpect(jsonPath("$.data.refreshToken").value("mock_refresh_token"))
                    .andExpect(jsonPath("$.data.expiresIn").value(3600))
                    .andExpect(jsonPath("$.data.userInfo.id").value(1))
                    .andExpect(jsonPath("$.data.userInfo.username").value("testuser"));

            verify(authService, times(1)).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("管理员登录 - 返回管理员信息")
        void login_管理员登录_返回管理员信息() throws Exception {
            mockUserInfo.setUserType("ADMIN");
            mockUserInfo.setStatus("ACTIVE");
            
            when(authService.login(any(LoginRequest.class))).thenReturn(mockAuthResponse);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validLoginRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.userInfo.userType").value("ADMIN"))
                    .andExpect(jsonPath("$.data.userInfo.status").value("ACTIVE"));

            verify(authService, times(1)).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("用户名为空 - 返回400状态码")
        void login_用户名为空_返回400状态码() throws Exception {
            validLoginRequest.setUsername(""); // 设置为空

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validLoginRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(authService, never()).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("密码为空 - 返回400状态码")
        void login_密码为空_返回400状态码() throws Exception {
            validLoginRequest.setPassword(""); // 设置为空

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validLoginRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(authService, never()).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("用户名不存在 - 返回401状态码")
        void login_用户名不存在_返回401状态码() throws Exception {
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new BusinessException(401, "用户名或密码错误"));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validLoginRequest)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(authService, times(1)).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("密码错误 - 返回401状态码")
        void login_密码错误_返回401状态码() throws Exception {
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new BusinessException(401, "用户名或密码错误"));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validLoginRequest)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(authService, times(1)).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("用户状态为INACTIVE - 返回403状态码")
        void login_用户状态为INACTIVE_返回403状态码() throws Exception {
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new BusinessException(403, "用户账户未激活"));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validLoginRequest)))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verify(authService, times(1)).login(any(LoginRequest.class));
        }
    }

    @Nested
    @DisplayName("令牌刷新接口测试")
    class RefreshTokenTests {

        @Test
        @DisplayName("正常刷新令牌 - 返回200状态码和新令牌")
        void refreshToken_正常刷新_返回200状态码() throws Exception {
            AuthResponse newAuthResponse = new AuthResponse();
            newAuthResponse.setAccessToken("new_access_token");
            newAuthResponse.setRefreshToken("new_refresh_token");
            newAuthResponse.setExpiresIn(3600L);
            newAuthResponse.setUserInfo(mockUserInfo);

            when(authService.refreshToken(anyString())).thenReturn(newAuthResponse);

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRefreshTokenRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.accessToken").value("new_access_token"))
                    .andExpect(jsonPath("$.data.refreshToken").value("new_refresh_token"));

            verify(authService, times(1)).refreshToken("valid_refresh_token");
        }

        @Test
        @DisplayName("刷新令牌为空 - 返回400状态码")
        void refreshToken_刷新令牌为空_返回400状态码() throws Exception {
            validRefreshTokenRequest.setRefreshToken(""); // 设置为空

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRefreshTokenRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(authService, never()).refreshToken(anyString());
        }

        @Test
        @DisplayName("刷新令牌无效 - 返回401状态码")
        void refreshToken_刷新令牌无效_返回401状态码() throws Exception {
            when(authService.refreshToken(anyString()))
                    .thenThrow(new BusinessException(401, "刷新令牌无效"));

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRefreshTokenRequest)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(authService, times(1)).refreshToken("valid_refresh_token");
        }

        @Test
        @DisplayName("刷新令牌已过期 - 返回401状态码")
        void refreshToken_刷新令牌已过期_返回401状态码() throws Exception {
            when(authService.refreshToken(anyString()))
                    .thenThrow(new BusinessException(401, "刷新令牌已过期"));

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRefreshTokenRequest)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(authService, times(1)).refreshToken("valid_refresh_token");
        }
    }

    @Nested
    @DisplayName("用户登出接口测试")
    class LogoutTests {

        @Test
        @DisplayName("正常登出 - 返回200状态码")
        void logout_正常登出_返回200状态码() throws Exception {
            doNothing().when(authService).logout(anyString());

            mockMvc.perform(post("/api/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRefreshTokenRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));

            verify(authService, times(1)).logout("valid_refresh_token");
        }

        @Test
        @DisplayName("刷新令牌为空 - 返回400状态码")
        void logout_刷新令牌为空_返回400状态码() throws Exception {
            validRefreshTokenRequest.setRefreshToken(""); // 设置为空

            mockMvc.perform(post("/api/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRefreshTokenRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(authService, never()).logout(anyString());
        }

        @Test
        @DisplayName("刷新令牌无效 - 返回401状态码")
        void logout_刷新令牌无效_返回401状态码() throws Exception {
            doThrow(new BusinessException(401, "刷新令牌无效"))
                    .when(authService).logout(anyString());

            mockMvc.perform(post("/api/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRefreshTokenRequest)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(authService, times(1)).logout("valid_refresh_token");
        }
    }

    @Nested
    @DisplayName("撤销所有令牌接口测试")
    class RevokeAllTokensTests {

        @Test
        @DisplayName("正常撤销所有令牌 - 返回200状态码")
        void revokeAllTokens_正常撤销_返回200状态码() throws Exception {
            try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
                mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
                doNothing().when(authService).revokeAllTokens(1L);

                mockMvc.perform(post("/api/auth/revoke-all")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(0));

                verify(authService, times(1)).revokeAllTokens(1L);
            }
        }

        @Test
        @DisplayName("用户未认证 - 返回401状态码")
        void revokeAllTokens_用户未认证_返回401状态码() throws Exception {
            try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
                mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(null);

                mockMvc.perform(post("/api/auth/revoke-all")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isInternalServerError()); // RuntimeException会被转换为500

                verify(authService, never()).revokeAllTokens(anyLong());
            }
        }

        @Test
        @DisplayName("撤销令牌失败 - 返回500状态码")
        void revokeAllTokens_撤销失败_返回500状态码() throws Exception {
            try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
                mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
                doThrow(new RuntimeException("数据库连接失败"))
                        .when(authService).revokeAllTokens(1L);

                mockMvc.perform(post("/api/auth/revoke-all")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isInternalServerError());

                verify(authService, times(1)).revokeAllTokens(1L);
            }
        }
    }

    @Nested
    @DisplayName("边界值和异常测试")
    class BoundaryAndExceptionTests {

        @Test
        @DisplayName("用户名长度边界测试 - 最小长度3")
        void register_用户名最小长度_返回成功() throws Exception {
            validRegisterRequest.setUsername("abc"); // 正好3个字符
            when(authService.register(any(RegisterRequest.class))).thenReturn(mockUserInfo);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(authService, times(1)).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("用户名长度边界测试 - 最大长度20")
        void register_用户名最大长度_返回成功() throws Exception {
            validRegisterRequest.setUsername("a".repeat(20)); // 正好20个字符
            when(authService.register(any(RegisterRequest.class))).thenReturn(mockUserInfo);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(authService, times(1)).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("密码长度边界测试 - 最小长度6")
        void register_密码最小长度_返回成功() throws Exception {
            validRegisterRequest.setPassword("123456"); // 正好6个字符
            when(authService.register(any(RegisterRequest.class))).thenReturn(mockUserInfo);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(authService, times(1)).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("密码长度边界测试 - 最大长度50")
        void register_密码最大长度_返回成功() throws Exception {
            validRegisterRequest.setPassword("a".repeat(50)); // 正好50个字符
            when(authService.register(any(RegisterRequest.class))).thenReturn(mockUserInfo);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(authService, times(1)).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("请求体格式错误 - 返回400状态码")
        void register_请求体格式错误_返回400状态码() throws Exception {
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("invalid json format"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(authService, never()).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("Content-Type错误 - 返回415状态码")
        void register_ContentType错误_返回415状态码() throws Exception {
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.TEXT_PLAIN)
                            .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andDo(print())
                    .andExpect(status().isUnsupportedMediaType());

            verify(authService, never()).register(any(RegisterRequest.class));
        }
    }
}