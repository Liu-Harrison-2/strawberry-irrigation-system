package com.strawberry.irrigation.module_user.service.impl;

import com.strawberry.irrigation.common.exception.BusinessException;
import com.strawberry.irrigation.module_user.dao.UserMapper;
import com.strawberry.irrigation.module_user.dto.UserCreateRequest;
import com.strawberry.irrigation.module_user.dto.UserResponse;
import com.strawberry.irrigation.module_user.dto.UserUpdateRequest;
import com.strawberry.irrigation.module_user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 用户服务实现类测试
 * 测试用户管理相关的业务逻辑
 * 
 * 测试覆盖范围：
 * 1. 用户创建业务逻辑测试
 * 2. 用户查询业务逻辑测试
 * 3. 用户更新业务逻辑测试
 * 4. 用户删除业务逻辑测试
 * 5. 数据校验逻辑测试
 * 6. 异常处理测试
 * 7. 边界条件测试
 */
@SpringBootTest(classes = UserServiceImpl.class)
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
                                  "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                                  "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration"
})
@Import({UserServiceImpl.class})
@DisplayName("用户服务实现类测试")
class UserServiceImplTest {

    @Autowired
    private UserServiceImpl userService;

    // 模拟数据库操作，避免真实数据库影响
    @MockBean
    private UserMapper userMapper;

    private UserCreateRequest validCreateRequest;
    private UserUpdateRequest validUpdateRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        // 准备有效的用户创建请求数据
        validCreateRequest = new UserCreateRequest();
        validCreateRequest.setUsername("testuser001");
        validCreateRequest.setPassword("123456");
        validCreateRequest.setRealName("测试用户");
        validCreateRequest.setPhoneNumber("13812345678");
        validCreateRequest.setUserType("FARMER");
        validCreateRequest.setEmail("test@example.com");
        validCreateRequest.setRemark("测试用户备注");

        // 准备有效的用户更新请求数据
        validUpdateRequest = new UserUpdateRequest();
        validUpdateRequest.setRealName("更新后的姓名");
        validUpdateRequest.setPhoneNumber("13999999999");
        validUpdateRequest.setEmail("updated@example.com");
        validUpdateRequest.setRemark("更新后的备注");

        // 准备模拟用户实体数据
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser001");
        mockUser.setPasswordHash("123456");
        mockUser.setRealName("测试用户");
        mockUser.setPhoneNumber("13812345678");
        mockUser.setUserType("FARMER");
        mockUser.setEmail("test@example.com");
        mockUser.setStatus("INACTIVE");
        mockUser.setCreatedAt(OffsetDateTime.now());
        mockUser.setUpdatedAt(OffsetDateTime.now());
        mockUser.setRemark("测试用户备注");
    }

    @Nested
    @DisplayName("用户创建业务逻辑测试")
    class CreateUserBusinessLogicTests {

        @Test
        @DisplayName("正常创建用户 - 成功")
        void createUser_正常创建用户_成功() {
            // 模拟数据库行为
            when(userMapper.selectCount(any())).thenReturn(0L); // 模拟用户名不存在
            when(userMapper.insert(any(User.class))).thenReturn(1); // 模拟插入成功

            // 执行测试
            UserResponse response = userService.createUser(validCreateRequest);

            // 验证结果
            assertNotNull(response);
            assertEquals("testuser001", response.getUsername());
            assertEquals("测试用户", response.getRealName());
            assertEquals("13812345678", response.getPhone());
            assertEquals("FARMER", response.getUserType());
            
            // 验证Mock调用
            verify(userMapper, times(3)).selectCount(any()); // 检查用户名、邮箱、手机号
            verify(userMapper, times(1)).insert(any(User.class));
        }

        @Test
        @DisplayName("创建用户时用户名已存在 - 抛出异常")
        void createUser_用户名已存在_抛出异常() {
            // 模拟用户名已存在
            when(userMapper.selectCount(any())).thenReturn(1L);

            // 验证是否抛出异常
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.createUser(validCreateRequest));

            assertTrue(exception.getMessage().contains("已存在"));
            verify(userMapper, times(1)).selectCount(any());
            verify(userMapper, never()).insert(any(User.class));
        }

        @Test
        @DisplayName("创建用户时邮箱已存在 - 抛出异常")
        void createUser_邮箱已存在_抛出异常() {
            // 模拟用户名不存在，但邮箱已存在
            when(userMapper.selectCount(any()))
                    .thenReturn(0L)  // 用户名不存在
                    .thenReturn(1L); // 邮箱已存在

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.createUser(validCreateRequest));

            assertTrue(exception.getMessage().contains("邮箱"));
            assertTrue(exception.getMessage().contains("已存在"));
            verify(userMapper, times(2)).selectCount(any());
            verify(userMapper, never()).insert(any(User.class));
        }

        @Test
        @DisplayName("创建用户时手机号已存在 - 抛出异常")
        void createUser_手机号已存在_抛出异常() {
            // 模拟用户名和邮箱不存在，但手机号已存在
            when(userMapper.selectCount(any()))
                    .thenReturn(0L)  // 用户名不存在
                    .thenReturn(0L)  // 邮箱不存在
                    .thenReturn(1L); // 手机号已存在

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.createUser(validCreateRequest));

            assertTrue(exception.getMessage().contains("手机号"));
            assertTrue(exception.getMessage().contains("已存在"));
            verify(userMapper, times(3)).selectCount(any());
            verify(userMapper, never()).insert(any(User.class));
        }

        @Test
        @DisplayName("创建用户时用户类型无效 - 抛出异常")
        void createUser_用户类型无效_抛出异常() {
            validCreateRequest.setUserType("INVALID_TYPE");

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.createUser(validCreateRequest));

            assertTrue(exception.getMessage().contains("用户类型"));
            verify(userMapper, never()).selectCount(any());
            verify(userMapper, never()).insert(any(User.class));
        }

        @Test
        @DisplayName("创建用户时手机号格式错误 - 抛出异常")
        void createUser_手机号格式错误_抛出异常() {
            validCreateRequest.setPhoneNumber("12345678901"); // 不以1开头

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.createUser(validCreateRequest));

            assertTrue(exception.getMessage().contains("手机号格式"));
            verify(userMapper, never()).selectCount(any());
            verify(userMapper, never()).insert(any(User.class));
        }

        @Test
        @DisplayName("创建用户不提供邮箱和手机号 - 成功")
        void createUser_不提供邮箱和手机号_成功() {
            validCreateRequest.setEmail(null);
            validCreateRequest.setPhoneNumber(null);

            when(userMapper.selectCount(any())).thenReturn(0L); // 用户名不存在
            when(userMapper.insert(any(User.class))).thenReturn(1);

            UserResponse response = userService.createUser(validCreateRequest);

            assertNotNull(response);
            assertEquals("testuser001", response.getUsername());
            verify(userMapper, times(1)).selectCount(any()); // 只检查用户名
            verify(userMapper, times(1)).insert(any(User.class));
        }
    }

    @Nested
    @DisplayName("用户查询业务逻辑测试")
    class GetUserBusinessLogicTests {

        @Test
        @DisplayName("根据ID查询用户存在 - 返回用户信息")
        void getUserById_用户存在_返回用户信息() {
            when(userMapper.selectById(1L)).thenReturn(mockUser);

            UserResponse response = userService.getUserById(1L);

            assertNotNull(response);
            assertEquals(1L, response.getId());
            assertEquals("testuser001", response.getUsername());
            assertEquals("测试用户", response.getRealName());
            verify(userMapper, times(1)).selectById(1L);
        }

        @Test
        @DisplayName("根据ID查询用户不存在 - 抛出异常")
        void getUserById_用户不存在_抛出异常() {
            when(userMapper.selectById(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class, 
                    () -> userService.getUserById(999L));
            
            assertTrue(exception.getMessage().contains("用户不存在"));
            verify(userMapper, times(1)).selectById(999L);
        }

        @Test
        @DisplayName("根据用户名查询用户存在 - 返回用户信息")
        void getUserByUsername_用户存在_返回用户信息() {
            when(userMapper.selectOne(any())).thenReturn(mockUser);

            UserResponse response = userService.getUserByUsername("testuser001");

            assertNotNull(response);
            assertEquals("testuser001", response.getUsername());
            verify(userMapper, times(1)).selectOne(any());
        }

        @Test
        @DisplayName("根据用户名查询用户不存在 - 抛出异常")
        void getUserByUsername_用户不存在_抛出异常() {
            when(userMapper.selectOne(any())).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class, 
                    () -> userService.getUserByUsername("nonexistent"));
            
            assertTrue(exception.getMessage().contains("用户不存在"));
            verify(userMapper, times(1)).selectOne(any());
        }

        @Test
        @DisplayName("获取所有用户列表 - 返回用户列表")
        void getAllUsers_正常查询_返回用户列表() {
            List<User> userList = Arrays.asList(mockUser);
            when(userMapper.selectList(null)).thenReturn(userList);

            List<UserResponse> responses = userService.getAllUsers();

            assertNotNull(responses);
            assertEquals(1, responses.size());
            assertEquals("testuser001", responses.get(0).getUsername());
            verify(userMapper, times(1)).selectList(null);
        }

        @Test
        @DisplayName("获取所有用户列表为空 - 返回空列表")
        void getAllUsers_空列表_返回空列表() {
            when(userMapper.selectList(null)).thenReturn(Collections.emptyList());

            List<UserResponse> responses = userService.getAllUsers();

            assertNotNull(responses);
            assertTrue(responses.isEmpty());
            verify(userMapper, times(1)).selectList(null);
        }

        @Test
        @DisplayName("分页查询用户正常参数 - 返回分页结果")
        void getUserPage_正常参数_返回分页结果() {
            List<User> userList = Arrays.asList(mockUser);
            when(userMapper.selectPage(any(), any())).thenAnswer(invocation -> {
                var page = invocation.getArgument(0, com.baomidou.mybatisplus.extension.plugins.pagination.Page.class);
                page.setRecords(userList);
                return page;
            });

            List<UserResponse> responses = userService.getUserPage(1, 10);

            assertNotNull(responses);
            assertEquals(1, responses.size());
            verify(userMapper, times(1)).selectPage(any(), any());
        }

        @Test
        @DisplayName("分页查询用户无效页码 - 抛出异常")
        void getUserPage_无效页码_抛出异常() {
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.getUserPage(0, 10));

            assertTrue(exception.getMessage().contains("页码必须大于0"));
            verify(userMapper, never()).selectPage(any(), any());
        }

        @Test
        @DisplayName("分页查询用户无效大小 - 抛出异常")
        void getUserPage_无效大小_抛出异常() {
            BusinessException exception1 = assertThrows(BusinessException.class,
                    () -> userService.getUserPage(1, 0));
            BusinessException exception2 = assertThrows(BusinessException.class,
                    () -> userService.getUserPage(1, 101));

            assertTrue(exception1.getMessage().contains("每页大小必须在1-100之间"));
            assertTrue(exception2.getMessage().contains("每页大小必须在1-100之间"));
            verify(userMapper, never()).selectPage(any(), any());
        }

        @Test
        @DisplayName("根据用户类型查询有效类型 - 返回用户列表")
        void getUsersByType_有效类型_返回用户列表() {
            List<User> farmerList = Arrays.asList(mockUser);
            when(userMapper.selectList(any())).thenReturn(farmerList);

            List<UserResponse> responses = userService.getUsersByType("FARMER");

            assertNotNull(responses);
            assertEquals(1, responses.size());
            assertEquals("FARMER", responses.get(0).getUserType());
            verify(userMapper, times(1)).selectList(any());
        }

        @Test
        @DisplayName("根据用户类型查询无效类型 - 抛出异常")
        void getUsersByType_无效类型_抛出异常() {
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.getUsersByType("INVALID_TYPE"));

            assertTrue(exception.getMessage().contains("无效的用户类型"));
            verify(userMapper, never()).selectList(any());
        }

        @Test
        @DisplayName("获取用户总数 - 返回数量")
        void getUserCount_正常查询_返回数量() {
            when(userMapper.selectCount(null)).thenReturn(10L);

            long count = userService.getUserCount();

            assertEquals(10L, count);
            verify(userMapper, times(1)).selectCount(null);
        }

        @Test
        @DisplayName("检查用户名存在 - 返回true")
        void isUsernameExists_用户名存在_返回true() {
            when(userMapper.selectCount(any())).thenReturn(1L);

            boolean exists = userService.isUsernameExists("testuser");

            assertTrue(exists);
            verify(userMapper, times(1)).selectCount(any());
        }

        @Test
        @DisplayName("检查用户名不存在 - 返回false")
        void isUsernameExists_用户名不存在_返回false() {
            when(userMapper.selectCount(any())).thenReturn(0L);

            boolean exists = userService.isUsernameExists("nonexistent");

            assertFalse(exists);
            verify(userMapper, times(1)).selectCount(any());
        }

        @Test
        @DisplayName("检查手机号存在 - 返回true")
        void isPhoneExists_手机号存在_返回true() {
            when(userMapper.selectCount(any())).thenReturn(1L);

            boolean exists = userService.isPhoneExists("13812345678");

            assertTrue(exists);
            verify(userMapper, times(1)).selectCount(any());
        }
    }

    @Nested
    @DisplayName("用户更新业务逻辑测试")
    class UpdateUserBusinessLogicTests {

        @Test
        @DisplayName("正常更新用户 - 成功")
        void updateUser_正常更新_成功() {
            // 模拟用户存在
            when(userMapper.selectById(1L)).thenReturn(mockUser);
            // 模拟手机号和邮箱不冲突
            when(userMapper.selectCount(any())).thenReturn(0L);
            // 模拟更新成功
            when(userMapper.update(isNull(), any())).thenReturn(1);
            // 模拟更新后的用户数据
            User updatedUser = new User();
            updatedUser.setId(mockUser.getId());
            updatedUser.setUsername(mockUser.getUsername());
            updatedUser.setRealName("更新后的姓名");
            updatedUser.setPhoneNumber(mockUser.getPhoneNumber());
            updatedUser.setUserType(mockUser.getUserType());
            updatedUser.setEmail(mockUser.getEmail());
            updatedUser.setStatus(mockUser.getStatus());
            updatedUser.setCreatedAt(mockUser.getCreatedAt());
            updatedUser.setUpdatedAt(mockUser.getUpdatedAt());
            when(userMapper.selectById(1L)).thenReturn(mockUser, updatedUser);

            UserResponse response = userService.updateUser(1L, validUpdateRequest);

            assertNotNull(response);
            verify(userMapper, times(2)).selectById(1L); // 更新前后各查询一次
            verify(userMapper, times(1)).update(isNull(), any());
        }

        @Test
        @DisplayName("更新不存在的用户 - 抛出异常")
        void updateUser_用户不存在_抛出异常() {
            when(userMapper.selectById(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.updateUser(999L, validUpdateRequest));

            assertTrue(exception.getMessage().contains("用户不存在"));
            verify(userMapper, times(1)).selectById(999L);
            verify(userMapper, never()).update(any(), any());
        }

        @Test
        @DisplayName("更新用户手机号冲突 - 抛出异常")
        void updateUser_手机号冲突_抛出异常() {
            mockUser.setPhoneNumber("13800000000"); // 原手机号
            validUpdateRequest.setPhoneNumber("13999999999"); // 新手机号
            
            when(userMapper.selectById(1L)).thenReturn(mockUser);
            when(userMapper.selectCount(any())).thenReturn(1L); // 手机号已存在

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.updateUser(1L, validUpdateRequest));

            assertTrue(exception.getMessage().contains("手机号"));
            assertTrue(exception.getMessage().contains("已被其他用户使用"));
            verify(userMapper, never()).update(any(), any());
        }

        @Test
        @DisplayName("更新用户邮箱冲突 - 抛出异常")
        void updateUser_邮箱冲突_抛出异常() {
            mockUser.setEmail("old@example.com"); // 原邮箱
            validUpdateRequest.setEmail("new@example.com"); // 新邮箱
            
            when(userMapper.selectById(1L)).thenReturn(mockUser);
            when(userMapper.selectCount(any()))
                    .thenReturn(0L)  // 手机号不冲突
                    .thenReturn(1L); // 邮箱已存在

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.updateUser(1L, validUpdateRequest));

            assertTrue(exception.getMessage().contains("邮箱"));
            assertTrue(exception.getMessage().contains("已被其他用户使用"));
            verify(userMapper, never()).update(any(), any());
        }

        @Test
        @DisplayName("更新用户无数据变化 - 成功")
        void updateUser_无数据变化_成功() {
            UserUpdateRequest emptyRequest = new UserUpdateRequest();
            
            when(userMapper.selectById(1L)).thenReturn(mockUser);
            when(userMapper.update(isNull(), any())).thenReturn(1);

            UserResponse response = userService.updateUser(1L, emptyRequest);

            assertNotNull(response);
            verify(userMapper, times(2)).selectById(1L);
            verify(userMapper, times(1)).update(isNull(), any());
        }
    }

    @Nested
    @DisplayName("用户删除业务逻辑测试")
    class DeleteUserBusinessLogicTests {

        @Test
        @DisplayName("正常删除用户 - 成功")
        void deleteUser_正常删除_成功() {
            when(userMapper.selectById(1L)).thenReturn(mockUser);
            when(userMapper.deleteById(1L)).thenReturn(1);

            // 不应该抛出异常
            assertDoesNotThrow(() -> userService.deleteUser(1L));

            verify(userMapper, times(1)).selectById(1L);
            verify(userMapper, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("删除不存在的用户 - 抛出异常")
        void deleteUser_用户不存在_抛出异常() {
            when(userMapper.selectById(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.deleteUser(999L));

            assertTrue(exception.getMessage().contains("用户不存在"));
            verify(userMapper, times(1)).selectById(999L);
            verify(userMapper, never()).deleteById(anyLong());
        }
    }

    @Nested
    @DisplayName("特殊业务逻辑测试")
    class SpecialBusinessLogicTests {

        @Test
        @DisplayName("根据用户名或邮箱查找用户 - 成功")
        void findByUsernameOrEmail_用户存在_返回用户() {
            when(userMapper.selectOne(any())).thenReturn(mockUser);

            User result = userService.findByUsernameOrEmail("testuser001");

            assertNotNull(result);
            assertEquals("testuser001", result.getUsername());
            verify(userMapper, times(1)).selectOne(any());
        }

        @Test
        @DisplayName("根据用户名或邮箱查找用户 - 用户不存在")
        void findByUsernameOrEmail_用户不存在_返回null() {
            when(userMapper.selectOne(any())).thenReturn(null);

            User result = userService.findByUsernameOrEmail("nonexistent");

            assertNull(result);
            verify(userMapper, times(1)).selectOne(any());
        }
    }
}