# 授权管理模块API测试执行指南

## 📋 模块概述
授权管理模块负责用户认证、授权、令牌管理等核心安全功能，包括：
- 🔐 **用户注册**: 新用户账户创建
- 🔑 **用户登录**: 身份验证和令牌颁发
- 🔄 **令牌刷新**: 延长用户会话时间
- 🚪 **用户登出**: 安全退出和令牌撤销
- 🛡️ **撤销所有令牌**: 安全防护功能

## 快速开始

### 1. 启动应用
```bash
cd "D:\My_Code_Project\Smart Irrigation System\backend"
mvn spring-boot:run
```

### 2. 验证应用启动成功
访问: http://localhost:8080/api/system/health
期望返回: `{"code":0,"message":"操作成功","data":{"status":"UP"}}`

## 测试工具选择

### 方案一：Postman（推荐）⭐
1. **导入测试集合**
   - 打开Postman
   - 点击 "Import" 
   - 选择文件: `docs/授权管理模块测试用例/授权管理API测试集合.postman_collection.json`
   - 导入成功后可看到完整的测试用例集合

2. **执行测试**
   - 单个测试：点击请求名称 → Send
   - 批量测试：点击集合名称 → Run collection
   - 自动化测试：使用 Newman 命令行工具

3. **测试环境变量**
   - `baseUrl`: http://localhost:8080
   - `accessToken`: 动态设置，登录后自动更新
   - `refreshToken`: 动态设置，登录后自动更新

### 方案二：VS Code REST Client
1. **安装插件**
   - 在VS Code中安装 "REST Client" 插件

2. **打开测试文件**
   - 打开文件: `docs/授权管理模块测试用例/授权管理API测试.http`
   - 点击每个请求上方的 "Send Request" 执行

3. **优点**
   - 轻量级，无需额外工具
   - 支持变量和脚本
   - 与代码编辑器集成

### 方案三：curl命令行
```bash
# 1. 用户注册
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "email": "test@example.com",
    "realName": "测试用户",
    "phoneNumber": "13800138000",
    "userType": "FARMER"
  }'

# 2. 用户登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'

# 3. 刷新令牌（需要先登录获取refreshToken）
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "your_refresh_token_here"
  }'

# 4. 用户登出
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "your_refresh_token_here"
  }'

# 5. 撤销所有令牌（需要JWT认证）
curl -X POST http://localhost:8080/api/auth/revoke-all \
  -H "Authorization: Bearer your_access_token_here" \
  -H "Content-Type: application/json"
```

## 详细测试场景

### 1. 用户注册接口测试 📝

#### 1.1 正常注册场景
```json
POST /api/auth/register
{
  "username": "farmer001",
  "password": "password123",
  "email": "farmer001@example.com",
  "realName": "张三",
  "phoneNumber": "13812345678",
  "userType": "FARMER"
}
```
**期望结果**: 201 Created，返回用户基本信息

#### 1.2 管理员注册场景
```json
POST /api/auth/register
{
  "username": "admin001",
  "password": "admin123456",
  "email": "admin@example.com",
  "realName": "管理员",
  "phoneNumber": "13800138001",
  "userType": "ADMIN"
}
```
**期望结果**: 201 Created，用户类型为ADMIN

#### 1.3 字段验证错误场景
```bash
# 用户名太短
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "ab", "password": "123456", "email": "test@example.com", "realName": "测试", "phoneNumber": "13800138000"}'

# 密码太短
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "123", "email": "test@example.com", "realName": "测试", "phoneNumber": "13800138000"}'

# 邮箱格式错误
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "123456", "email": "invalid-email", "realName": "测试", "phoneNumber": "13800138000"}'

# 手机号格式错误
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "123456", "email": "test@example.com", "realName": "测试", "phoneNumber": "123"}'
```
**期望结果**: 400 Bad Request，返回具体的验证错误信息

#### 1.4 重复数据错误场景
```bash
# 用户名已存在
# 邮箱已存在  
# 手机号已存在
```
**期望结果**: 409 Conflict，返回冲突信息

### 2. 用户登录接口测试 🔑

#### 2.1 正常登录场景
```json
POST /api/auth/login
{
  "username": "farmer001",
  "password": "password123"
}
```
**期望结果**: 200 OK，返回认证信息
```json
{
  "code": 0,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "refresh_token_string",
    "expiresIn": 3600,
    "userInfo": {
      "id": 1,
      "username": "farmer001",
      "realName": "张三",
      "userType": "FARMER",
      "status": "ACTIVE"
    }
  }
}
```

#### 2.2 登录失败场景
```bash
# 用户名不存在
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "nonexistent", "password": "123456"}'

# 密码错误
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "farmer001", "password": "wrongpassword"}'

# 用户状态异常（INACTIVE）
# 需要先创建INACTIVE状态的用户进行测试
```
**期望结果**: 401 Unauthorized 或 403 Forbidden

### 3. 令牌刷新接口测试 🔄

#### 3.1 正常刷新场景
```json
POST /api/auth/refresh
{
  "refreshToken": "valid_refresh_token_from_login"
}
```
**期望结果**: 200 OK，返回新的认证信息

#### 3.2 刷新失败场景
```bash
# 刷新令牌无效
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "invalid_token"}'

# 刷新令牌为空
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": ""}'
```
**期望结果**: 400 Bad Request 或 401 Unauthorized

### 4. 用户登出接口测试 🚪

#### 4.1 正常登出场景
```json
POST /api/auth/logout
{
  "refreshToken": "valid_refresh_token"
}
```
**期望结果**: 200 OK，令牌被撤销

#### 4.2 登出失败场景
```bash
# 刷新令牌无效
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "invalid_token"}'
```
**期望结果**: 401 Unauthorized

### 5. 撤销所有令牌接口测试 🛡️

#### 5.1 正常撤销场景
```bash
curl -X POST http://localhost:8080/api/auth/revoke-all \
  -H "Authorization: Bearer valid_access_token" \
  -H "Content-Type: application/json"
```
**期望结果**: 200 OK，所有令牌被撤销

#### 5.2 撤销失败场景
```bash
# 未提供认证令牌
curl -X POST http://localhost:8080/api/auth/revoke-all \
  -H "Content-Type: application/json"

# 无效的访问令牌
curl -X POST http://localhost:8080/api/auth/revoke-all \
  -H "Authorization: Bearer invalid_token" \
  -H "Content-Type: application/json"
```
**期望结果**: 401 Unauthorized

## 完整测试流程 🔄

### 标准测试流程
```bash
# 1. 注册新用户
POST /api/auth/register

# 2. 登录获取令牌
POST /api/auth/login
# 保存返回的 accessToken 和 refreshToken

# 3. 使用访问令牌访问受保护资源
GET /api/users (需要ADMIN权限)
Header: Authorization: Bearer {accessToken}

# 4. 刷新令牌
POST /api/auth/refresh
Body: {"refreshToken": "{refreshToken}"}

# 5. 登出
POST /api/auth/logout
Body: {"refreshToken": "{refreshToken}"}

# 6. 验证登出后令牌失效
POST /api/auth/refresh (应该失败)
```

### 安全测试流程
```bash
# 1. 令牌篡改测试
# 修改JWT令牌内容，验证是否被拒绝

# 2. 过期令牌测试
# 等待令牌过期或手动设置过期时间

# 3. 撤销令牌测试
# 撤销令牌后尝试使用，验证是否被拒绝

# 4. 并发登录测试
# 同一用户多次登录，验证令牌管理
```

## 测试数据管理 📊

### 测试用户数据
```json
{
  "farmer": {
    "username": "farmer001",
    "password": "password123",
    "email": "farmer@example.com",
    "realName": "农民用户",
    "phoneNumber": "13800138000",
    "userType": "FARMER"
  },
  "admin": {
    "username": "admin001", 
    "password": "admin123456",
    "email": "admin@example.com",
    "realName": "管理员用户",
    "phoneNumber": "13800138001",
    "userType": "ADMIN"
  },
  "technician": {
    "username": "tech001",
    "password": "tech123456", 
    "email": "tech@example.com",
    "realName": "技术员用户",
    "phoneNumber": "13800138002",
    "userType": "TECHNICIAN"
  }
}
```

### 清理测试数据
```sql
-- 清理测试用户（如果需要）
DELETE FROM users WHERE username LIKE 'test%' OR username LIKE 'farmer%';
DELETE FROM refresh_tokens WHERE user_id NOT IN (SELECT id FROM users);
```

## 常见问题排查 🔧

### 1. 应用启动失败
- 检查端口8080是否被占用
- 检查数据库连接配置
- 查看启动日志错误信息

### 2. 认证失败
- 确认用户已注册且状态为ACTIVE
- 检查密码是否正确
- 验证JWT配置是否正确

### 3. 令牌相关问题
- 检查令牌是否过期
- 验证令牌格式是否正确
- 确认令牌未被撤销

### 4. 权限问题
- 确认用户类型和权限配置
- 检查Spring Security配置
- 验证角色映射是否正确

## 自动化测试 🤖

### Newman命令行执行
```bash
# 安装Newman
npm install -g newman

# 执行Postman集合
newman run "授权管理API测试集合.postman_collection.json" \
  --environment "test-environment.json" \
  --reporters cli,html \
  --reporter-html-export "test-report.html"
```

### 持续集成配置
```yaml
# GitHub Actions 示例
name: API Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Setup Java
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Start Application
        run: mvn spring-boot:run &
      - name: Wait for Application
        run: sleep 30
      - name: Run API Tests
        run: newman run postman-collection.json
```

---

**文档版本**: v1.0  
**创建日期**: 2024-01-XX  
**最后更新**: 2024-01-XX  
**维护人员**: 开发团队