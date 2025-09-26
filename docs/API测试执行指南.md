# 用户管理模块API测试执行指南

## 快速开始

### 1. 启动应用
```bash
cd "d:\My_Code_Project\Smart Irrigation System\backend"
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
   - 选择文件: `docs/用户管理API测试集合.postman_collection.json`
   - 导入成功后可看到69个测试用例

2. **执行测试**
   - 单个测试：点击请求名称 → Send
   - 批量测试：点击集合名称 → Run collection
   - 自动化测试：使用 Newman 命令行工具

3. **测试环境变量**
   - `baseUrl`: http://localhost:8080
   - `userId`: 动态设置，根据创建用户返回的ID更新

### 方案二：VS Code REST Client
1. **安装插件**
   - 在VS Code中安装 "REST Client" 插件

2. **打开测试文件**
   - 打开文件: `docs/用户管理API测试.http`
   - 点击每个请求上方的 "Send Request" 执行

3. **优点**
   - 轻量级，无需额外工具
   - 支持变量和脚本
   - 与代码编辑器集成

### 方案三：curl命令行
```bash
# 创建用户
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "farmer001",
    "password": "123456", 
    "realName": "张三",
    "phoneNumber": "13812345678",
    "userType": "FARMER",
    "email": "farmer001@example.com"
  }'

# 查询用户
curl -X GET http://localhost:8080/api/users/1

# 更新用户
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "realName": "张三丰",
    "phoneNumber": "13900000000"
  }'

# 删除用户  
curl -X DELETE http://localhost:8080/api/users/1
```

## 测试执行建议顺序

### 阶段一：基础功能验证（必须）
1. **健康检查** - 确保服务正常运行
2. **创建用户** - 验证基本创建功能
3. **查询用户** - 验证数据正确保存
4. **更新用户** - 验证更新功能
5. **删除用户** - 验证删除功能

### 阶段二：参数验证测试（重要）
1. **创建用户参数错误** - 验证输入校验
2. **查询不存在用户** - 验证错误处理
3. **更新冲突数据** - 验证业务规则
4. **分页参数错误** - 验证边界条件

### 阶段三：业务场景测试（推荐）
1. **完整生命周期** - 验证端到端流程
2. **唯一性约束** - 验证数据完整性
3. **特殊字符处理** - 验证数据兼容性
4. **大数据量查询** - 验证性能表现

## 测试检查清单

### 功能性测试 ✅
- [ ] 所有CRUD操作正常工作
- [ ] 参数验证正确触发
- [ ] 业务规则（唯一性约束）正确执行
- [ ] 错误信息清晰准确
- [ ] HTTP状态码正确返回

### 数据完整性测试 ✅  
- [ ] 创建的数据能正确保存和查询
- [ ] 更新操作不影响其他字段
- [ ] 删除操作彻底清除数据
- [ ] 关联数据处理正确

### 安全性测试 ✅
- [ ] 敏感信息（如密码）不在响应中暴露
- [ ] SQL注入防护有效
- [ ] 输入验证阻止恶意数据

### 性能测试 ✅
- [ ] 响应时间在可接受范围内（<500ms）
- [ ] 大数据量查询性能合理
- [ ] 并发请求处理正常

### 兼容性测试 ✅
- [ ] 特殊字符处理正确
- [ ] Unicode字符支持良好
- [ ] 不同客户端工具兼容

## 常见问题及解决方案

### 问题1：连接被拒绝
**原因**: 应用未启动或端口占用
**解决**: 
```bash
# 检查应用状态
netstat -ano | findstr :8080
# 重新启动应用
mvn spring-boot:run
```

### 问题2：404 Not Found
**原因**: URL路径错误或接口不存在
**解决**: 检查URL拼写，确认接口已实现

### 问题3：500 Internal Server Error
**原因**: 服务器内部错误，通常是代码问题
**解决**: 查看应用日志，检查异常信息

### 问题4：400 Bad Request
**原因**: 请求参数错误或验证失败
**解决**: 检查请求体格式和字段值

### 问题5：数据库相关错误
**原因**: 数据库连接问题或表结构问题
**解决**: 检查数据库配置和表结构

## 测试报告模板

### 测试执行记录
```
测试日期: 2024-XX-XX
测试人员: [姓名]
测试环境: localhost:8080
测试工具: Postman/VS Code REST Client

执行结果:
✅ 基础功能测试: 通过 (X/X)
✅ 参数验证测试: 通过 (X/X)  
✅ 业务场景测试: 通过 (X/X)
✅ 异常处理测试: 通过 (X/X)

发现问题:
1. [问题描述] - [严重程度] - [状态]

性能数据:
- 平均响应时间: XXXms
- 创建用户: XXXms
- 查询用户: XXXms
- 更新用户: XXXms
- 删除用户: XXXms

建议:
1. [改进建议]
2. [优化建议]
```

## 自动化测试集成

### Newman命令行执行
```bash
# 安装Newman
npm install -g newman

# 执行Postman集合
newman run "docs/用户管理API测试集合.postman_collection.json" \
  --environment "postman_environment.json" \
  --reporters cli,html \
  --reporter-html-export newman-report.html
```

### CI/CD集成示例
```yaml
# GitHub Actions示例
- name: Run API Tests
  run: |
    mvn spring-boot:run &
    sleep 30
    newman run docs/用户管理API测试集合.postman_collection.json
    pkill -f spring-boot
```

通过遵循这个指南，你可以系统性地验证用户管理模块的所有API功能，确保系统的稳定性和可靠性。