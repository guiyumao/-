# 项目修复与中文用例添加计划

## 问题分析

经过深入探索，我发现了以下需要修复的问题：

### 1. 后端硬编码英文问题
- **ApiResponse.java**: 返回消息 "success"、错误消息都是硬编码英文
- **AuthServiceImpl.java**: 所有异常消息都是英文硬编码
  - "Registration role is not available"
  - "Laboratory is unavailable"
  - "Username already exists"
  - "User number already exists"
  - "Phone already exists"
  - "Email already exists"
  - "registered"
- **GlobalExceptionHandler.java**: 异常处理消息是英文
  - "request validation failed"
  - "Unhandled exception"
- **各个 Controller**: 操作成功消息硬编码
  - "created", "updated", "deleted", "returned", "sent"
- **Service 实现类**: 业务逻辑异常消息都是英文
  - Equipment/Consumable/Hazardous 相关服务

### 2. 缺少测试用例
- backend/src/test 目录不存在
- 没有单元测试
- 没有集成测试

### 3. 缺少示例数据
- 数据库初始化脚本只有表结构和权限数据
- 没有业务数据示例

### 4. 前端部分硬编码（虽然已经大部分中文化）
- LoginView.vue 中有 "Laboratory Resource Hub", "Sign In", "Register" 等英文

## 解决方案

### 阶段一：创建中文消息配置系统（避免硬编码）

1. **创建消息资源配置类**
   - 创建 `MessageConfig.java` 存储所有中文消息常量
   - 按模块分类：通用消息、认证消息、业务消息、验证消息

2. **重构后端代码使用消息配置**
   - 修改 `ApiResponse.java` 使用配置的中文消息
   - 修改 `AuthServiceImpl.java` 所有异常消息
   - 修改 `GlobalExceptionHandler.java` 异常处理消息
   - 修改所有 Controller 的成功消息
   - 修改所有 Service 实现类的业务异常消息

### 阶段二：添加完整的测试用例

1. **创建测试基础设施**
   - 添加测试依赖到 pom.xml（如果缺少）
   - 创建测试配置类
   - 创建测试数据工厂

2. **添加单元测试**
   - AuthService 测试（登录、注册、权限）
   - EquipmentService 测试（CRUD、借还）
   - ConsumableService 测试（入库、出库、库存预警）
   - HazardousService 测试（领用、归还、废液处理）
   - InventoryService 测试（库存计算、预警）

3. **添加集成测试**
   - Controller 层接口测试
   - 完整业务流程测试

### 阶段三：添加示例数据脚本

1. **创建示例数据SQL**
   - 实验室示例数据（3-5个实验室）
   - 用户示例数据（各角色用户）
   - 设备分类和设备数据
   - 耗材分类和耗材数据
   - 危化品数据
   - 借用记录示例
   - 维修记录示例
   - 校准记录示例

2. **创建数据导入脚本**
   - 提供一键导入示例数据的脚本
   - 提供清空示例数据的脚本

### 阶段四：前端完全中文化

1. **移除前端残留英文**
   - LoginView.vue 中的英文文案
   - 其他视图中可能存在的英文

## 实施步骤

### 步骤 1: 创建消息配置类
- 文件：`backend/src/main/java/edu/university/lab/common/constant/Messages.java`

### 步骤 2: 重构 ApiResponse
- 修改成功和失败消息为中文

### 步骤 3: 重构 AuthServiceImpl
- 所有异常消息改为中文常量

### 步骤 4: 重构 GlobalExceptionHandler
- 异常处理消息中文化

### 步骤 5: 重构所有 Controller
- 操作成功消息中文化

### 步骤 6: 重构所有 Service 实现类
- 业务异常消息中文化

### 步骤 7: 创建测试基础设施
- 添加测试依赖
- 创建测试配置

### 步骤 8: 添加单元测试
- 每个服务至少 5 个测试用例

### 步骤 9: 添加集成测试
- 每个 Controller 至少 3 个测试用例

### 步骤 10: 创建示例数据
- 创建 `db/sample_data.sql`

### 步骤 11: 前端中文化
- 修改 LoginView.vue 等文件

## 文件清单

### 需要创建的文件
1. `backend/src/main/java/edu/university/lab/common/constant/Messages.java`
2. `backend/src/test/java/edu/university/lab/auth/service/AuthServiceTest.java`
3. `backend/src/test/java/edu/university/lab/module/equipment/service/EquipmentServiceTest.java`
4. `backend/src/test/java/edu/university/lab/module/consumable/service/ConsumableServiceTest.java`
5. `backend/src/test/java/edu/university/lab/module/equipmentborrow/service/EquipmentBorrowServiceTest.java`
6. `backend/src/test/java/edu/university/lab/module/inventory/service/InventoryServiceTest.java`
7. `backend/src/test/java/edu/university/lab/auth/controller/AuthControllerTest.java`
8. `backend/src/test/java/edu/university/lab/module/equipment/controller/EquipmentControllerTest.java`
9. `backend/src/test/resources/application-test.yml`
10. `db/sample_data.sql`
11. `scripts/import-sample-data.ps1`
12. `scripts/clear-sample-data.ps1`

### 需要修改的文件（约 30+ 文件）
- `backend/src/main/java/edu/university/lab/common/api/ApiResponse.java`
- `backend/src/main/java/edu/university/lab/common/api/GlobalExceptionHandler.java`
- `backend/src/main/java/edu/university/lab/auth/service/impl/AuthServiceImpl.java`
- 所有 Controller 文件（约 15 个）
- 所有 Service 实现类文件（约 10 个）
- `frontend/src/views/login/LoginView.vue`
- `backend/pom.xml`（添加测试依赖）

## 预期效果

1. **完全中文化**：所有面向用户的消息都是中文，无硬编码英文
2. **消息集中管理**：所有消息在 Messages 类中统一管理，易于维护和修改
3. **完整测试覆盖**：核心业务逻辑有单元测试和集成测试
4. **示例数据**：提供真实的示例数据，便于演示和开发测试
5. **更好的可维护性**：代码质量提升，符合最佳实践

## 风险与注意事项

1. **测试数据库**：需要使用独立的测试数据库配置
2. **消息一致性**：确保前后端消息提示一致
3. **向后兼容**：确保修改不影响现有功能
4. **性能影响**：测试用例不应该影响生产代码性能
