# 实验室设备与耗材管理系统功能说明文档

## 1. 文档说明

本文档基于当前工程实际代码、已完成功能和最新数据库迁移整理，说明系统当前可运行、可演示、可联调的功能范围。

说明原则：
- 以当前项目实现为准
- 同时覆盖前端页面能力、后端接口能力和数据库对象
- 明确本轮新增优化内容

---

## 2. 系统概览

本系统面向高校实验室资产与业务场景，提供设备、耗材、危化品、库存、借还、维修、校准、提醒、报表和权限控制能力，采用前后端分离架构。

当前技术实现：
- 后端：Spring Boot 3 + Spring Security + JWT + MyBatis-Plus
- 前端：Vue 3 + TypeScript + Element Plus + ECharts
- 数据库：MySQL 8
- 缓存：Redis 7

系统入口：
- 前端：`http://127.0.0.1:5173`
- 后端：`http://127.0.0.1:8080/api`
- Swagger：`http://127.0.0.1:8080/swagger-ui.html`

---

## 3. 角色与权限体系

### 3.1 已配置角色

系统当前内置角色：
- `sys_admin`：系统管理员
- `lab_director`：实验室主任
- `equipment_admin`：设备管理员
- `consumable_admin`：耗材管理员
- `hazardous_admin`：危化品管理员
- `teacher`：教师
- `student`：学生
- `repair_staff`：维修人员
- `calibration_staff`：校准人员

### 3.2 权限控制方式

系统采用 JWT + RBAC 权限模型。

已实现权限层级：
- 接口级权限：后端使用 `@PreAuthorize`
- 菜单级权限：菜单来自数据库 `menu`
- 角色授权：来自数据库 `role_menu`
- 页面级权限：前端按菜单动态渲染侧栏与路由
- 按钮级权限：前端通过 `v-permission` 指令控制显示

### 3.3 当前典型权限点

主要权限编码包括：
- `dashboard:view`
- `laboratory:view` / `laboratory:edit`
- `user:view` / `user:edit`
- `role:view` / `role:edit`
- `menu:view`
- `equipment_category:view` / `equipment_category:edit`
- `equipment:view` / `equipment:edit`
- `equipment_borrow:view` / `equipment_borrow:edit`
- `equipment_repair:view` / `equipment_repair:edit`
- `equipment_calibration:view` / `equipment_calibration:edit`
- `consumable_category:view` / `consumable_category:edit`
- `consumable:view` / `consumable:edit`
- `consumable_inbound:view` / `consumable_inbound:edit`
- `consumable_outbound:view` / `consumable_outbound:edit`
- `hazardous_material:view` / `hazardous_material:edit`
- `hazardous_usage:view` / `hazardous_usage:edit`
- `inventory:view`
- `report:view` / `report:export`
- `audit:view`
- `reminder:view`

---

## 4. 前端功能模块说明

### 4.1 登录与会话管理

页面：
- `登录页`

功能：
- 用户名密码登录
- 登录成功后获取 JWT Token
- 自动获取当前用户信息、菜单树、权限列表
- 页面刷新后自动恢复登录上下文
- Token 失效时自动跳转登录页

---

### 4.2 仪表盘

页面：
- `仪表盘`

功能：
- 展示实验室数量
- 展示设备总数
- 展示耗材种类数量
- 展示危化品种类数量
- 展示当前借出数量
- 展示本月耗材成本
- 展示校准即将到期数量
- 展示耗材即将过期数量
- 展示近 6 日耗材趋势图

对应接口：
- `GET /api/dashboard/stats`

---

### 4.3 系统管理

#### 4.3.1 实验室管理

功能：
- 分页查询
- 关键字查询
- 新增
- 编辑
- 软删除

对应接口：
- `GET /api/laboratories`
- `GET /api/laboratories/{id}`
- `POST /api/laboratories`
- `PUT /api/laboratories/{id}`
- `DELETE /api/laboratories/{id}`

#### 4.3.2 用户管理

功能：
- 分页查询用户
- 新增用户
- 编辑用户
- 软删除用户
- 重置用户密码

对应接口：
- `GET /api/users`
- `GET /api/users/{id}`
- `POST /api/users`
- `PUT /api/users/{id}`
- `PUT /api/users/{id}/reset-password`
- `DELETE /api/users/{id}`

前端增强：
- 操作列新增“重置密码”按钮
- 采用弹窗输入新密码和确认密码
- 表单校验两次密码一致

#### 4.3.3 角色与菜单

功能：
- 查看角色列表
- 新增角色
- 编辑角色名称
- 查看菜单树
- 为角色分配菜单与按钮权限
- 菜单支持父子级分组

对应接口：
- `GET /api/roles`
- `POST /api/roles`
- `PUT /api/roles/{id}`
- `GET /api/roles/{id}/menu-ids`
- `POST /api/roles/{id}/menus`
- `GET /api/menus/tree`

#### 4.3.4 操作日志

页面：
- `操作日志`

功能：
- 查看审计日志分页列表
- 按时间范围筛选
- 展示操作人、操作描述、请求方法、IP、操作时间

对应接口：
- `GET /api/audit-logs`

---

### 4.4 资产档案管理

当前已实现基础 CRUD：
- 设备分类
- 设备台账
- 耗材分类
- 耗材台账
- 危化品台账

共同能力：
- 分页查询
- 单条详情
- 新增
- 编辑
- 软删除

---

### 4.5 业务流程管理

#### 4.5.1 设备借用

功能：
- 查询借用记录
- 提交借用申请
- 归还设备
- 录入归还情况
- 校验应还时间必须晚于借用时间

业务联动：
- 借出时设备状态改为借出
- 归还时设备状态恢复可用
- 已接入审计日志

对应接口：
- `GET /api/equipment-borrows`
- `GET /api/equipment-borrows/{id}`
- `POST /api/equipment-borrows`
- `PUT /api/equipment-borrows/{id}/return`
- `DELETE /api/equipment-borrows/{id}`

#### 4.5.2 耗材入库

功能：
- 查询入库记录
- 新增入库记录
- 维护批号、数量、单价、供应商、有效期

对应接口：
- `GET /api/consumable-inbounds`
- `POST /api/consumable-inbounds`

#### 4.5.3 耗材出库

功能：
- 查询出库记录
- 按实验室和耗材联动库存批次
- 校验数量不得超过可用库存
- 自动带出单价并计算金额
- 已接入审计日志

对应接口：
- `GET /api/consumable-outbounds`
- `POST /api/consumable-outbounds`
- `GET /api/inventory/options`

#### 4.5.4 危化品业务

功能：
- 统一登记危化品入库、领用、归还、废液处理
- 联动库存批次
- 校验领用与处置数量
- 已接入审计日志

对应接口：
- `GET /api/hazardous-usages`
- `POST /api/hazardous-usages`
- `GET /api/inventory/options`

#### 4.5.5 设备维修

功能：
- 查询维修记录
- 新增报修记录
- 更新维修状态
- 维护维修人员、维修时间、维修费用、维修结果
- 已接入审计日志

对应接口：
- `GET /api/equipment-repairs`
- `POST /api/equipment-repairs`
- `PUT /api/equipment-repairs/{id}/status`

#### 4.5.6 设备校准

功能：
- 查询校准记录
- 创建校准任务
- 确认校准结果
- 自动更新最近与下次校准日期
- 已接入审计日志

对应接口：
- `GET /api/equipment-calibrations`
- `POST /api/equipment-calibrations`
- `PUT /api/equipment-calibrations/{id}/confirm`

---

### 4.6 库存管理

#### 4.6.1 库存预警

页面：
- `库存预警`

功能：
- 查询低于安全库存的耗材
- 支持关键字查询和分页
- 库存不足行高亮显示

对应接口：
- `GET /api/inventory/alert`

#### 4.6.2 库存批次选项

功能：
- 为耗材出库和危化品业务提供可用库存批次联动数据

对应接口：
- `GET /api/inventory/options`

---

### 4.7 报表中心

页面：
- `报表中心`

功能：
- 查看业务汇总报表
- 按实验室筛选
- 按日期范围筛选
- 按业务类型筛选
- 支持 CSV 导出

支持业务类型：
- 设备借用
- 设备维修
- 设备校准
- 耗材出库
- 危化品业务

对应接口：
- `GET /api/reports/summary`
- `GET /api/reports/summary/export`

---

### 4.8 到期提醒

页面：
- `到期提醒`

功能：
- 查看校准即将到期设备
- 查看即将过期耗材批次
- 到期项使用红色标签突出显示

对应接口：
- `GET /api/reminders`

---

## 5. 后端接口模块清单

### 5.1 认证模块

- `POST /api/auth/login`
- `GET /api/auth/me`
- `GET /api/auth/context`

### 5.2 仪表盘模块

- `GET /api/dashboard/stats`

### 5.3 系统管理模块

- `GET /api/laboratories`
- `GET /api/users`
- `PUT /api/users/{id}/reset-password`
- `GET /api/roles`
- `POST /api/roles`
- `PUT /api/roles/{id}`
- `GET /api/roles/{id}/menu-ids`
- `POST /api/roles/{id}/menus`
- `GET /api/menus/tree`
- `GET /api/audit-logs`

### 5.4 业务模块

- 设备借用
- 耗材入库
- 耗材出库
- 危化品业务
- 设备维修
- 设备校准
- 库存预警
- 报表中心
- 到期提醒

---

## 6. 核心业务流程说明

### 6.1 设备借用与归还

1. 用户选择设备并填写借用信息
2. 系统校验设备必须可借
3. 保存借用记录并更新设备状态
4. 归还时录入归还情况
5. 更新借用记录和设备状态
6. 写入审计日志

### 6.2 耗材出库

1. 用户选择实验室和耗材
2. 系统联动库存批次
3. 校验数量不超过库存
4. 自动计算金额
5. 保存出库记录并写入审计日志

### 6.3 危化品业务

1. 用户选择危化品与业务类型
2. 非入库场景联动库存批次
3. 校验数量是否充足
4. 保存业务记录并写入审计日志

### 6.4 设备维修

1. 创建报修记录
2. 设备状态更新为维修中
3. 维修人员更新维修状态与结果
4. 完成后恢复设备状态或标记报废
5. 写入审计日志

### 6.5 设备校准

1. 创建校准任务
2. 校验有效期必须晚于校准时间
3. 更新设备状态为校准中
4. 确认校准结果
5. 自动更新最近与下次校准日期
6. 写入审计日志

---

## 7. 数据库对象补充说明

本轮已新增并导入数据库的对象：

### 7.1 表

- `sys_audit_log`

用途：
- 存储带 `@AuditLog` 注解的核心业务操作记录
- 存储触发器演示日志

### 7.2 视图

- `v_equipment_borrow_status`

用途：
- 联表展示设备借用概况
- 可供后续前端快速查询借用状态

### 7.3 存储过程

- `sp_consumable_yearly_stats(IN p_year INT)`

用途：
- 按年份统计各耗材分类和耗材年度领用数量

### 7.4 触发器

- `trg_after_borrow_return`

用途：
- 在设备归还时自动恢复设备状态
- 同时写入一条触发器审计日志

### 7.5 菜单种子

新增菜单与权限：
- 库存管理
- 库存预警
- 操作日志
- 到期提醒
- `audit:view`
- `reminder:view`

---

## 8. 本轮增量优化总结

相较上一版，本轮已新增：
- 用户密码重置功能
- 角色新增与编辑功能
- 库存预警页面与接口
- 报表多维筛选与导出
- 操作审计日志
- 到期提醒
- 数据库增量迁移脚本与对象

---

## 9. 当前交付结论

当前系统已具备：
- 完整的基础管理骨架
- 可运行的核心实验室业务闭环
- 可配置的菜单权限体系
- 可用的按钮级权限显示控制
- 可演示的库存预警、提醒、审计和报表导出能力

适合用于：
- 课程设计演示
- 毕业设计阶段性验收
- 实验室信息化项目原型交付
