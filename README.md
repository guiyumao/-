# 实验室设备与耗材管理系统

本项目是一个面向高校实验室场景的前后端分离管理系统，覆盖设备、耗材、危化品、库存、借还、维修、校准、提醒、报表和权限控制等核心能力。

当前技术栈：
- 后端：Spring Boot 3、Spring Security、JWT、MyBatis-Plus
- 前端：Vue 3、TypeScript、Element Plus、ECharts、Vite
- 数据库：MySQL 8
- 缓存：Redis 7

## 目录结构

```text
.
├─ backend
├─ frontend
├─ docker
├─ db
├─ docs
├─ scripts
├─ docker-compose.yml
├─ lab_management_system.sql
├─ start-dev.bat
├─ start-dev.ps1
└─ README.md
```

## 当前已实现功能

- JWT 登录鉴权与 RBAC 菜单权限控制
- 数据库驱动菜单、父子级菜单、按钮级权限显示控制
- 实验室、用户、设备分类、设备、耗材分类、耗材、危化品基础 CRUD
- 设备借用与归还
- 耗材入库、耗材出库
- 危化品领用、归还、废液处理登记
- 设备维修、设备校准
- 库存预警页面与接口
- 仪表盘统计图表与到期提醒
- 报表中心多维筛选与 CSV 导出
- 操作审计日志
- 数据库增量迁移脚本、视图、存储过程、触发器

## 一键启动

推荐直接使用根目录脚本启动开发环境。

### 方式一

双击：

```text
start-dev.bat
```

### 方式二

PowerShell 执行：

```powershell
.\start-dev.ps1
```

脚本会自动完成：
- 启动 Docker 中的 MySQL 和 Redis
- 等待 MySQL 健康检查通过
- 分别打开后端和前端开发窗口

## 默认访问地址

- 前端：`http://127.0.0.1:5173`
- 后端：`http://127.0.0.1:8080/api`
- Swagger：`http://127.0.0.1:8080/swagger-ui.html`

## 默认测试账号

- 用户名：`admin`
- 密码：`admin123`

## 手动启动

### 1. 启动依赖

```powershell
docker compose up -d
```

### 2. 启动后端

```powershell
cd .\backend
.\mvnw.cmd spring-boot:run
```

### 3. 启动前端

```powershell
cd .\frontend
npm run dev -- --host=127.0.0.1 --port=5173
```

## 数据库说明

- 数据库名：`lab_management_system`
- 地址：`127.0.0.1:3306`
- 用户名：`root`
- 密码：`root123456`

### 初始化整库

如需重新导入基础数据库：

```powershell
docker cp .\lab_management_system.sql lab-management-mysql:/tmp/lab_management_system.sql
docker exec lab-management-mysql sh -lc "mysql --default-character-set=utf8mb4 -uroot -proot123456 < /tmp/lab_management_system.sql"
```

### 导入增量迁移

当前最新增量脚本：

```text
db/migration/V20260529_01_feature_enhancement.sql
```

导入命令：

```powershell
docker cp .\db\migration\V20260529_01_feature_enhancement.sql lab-management-mysql:/tmp/V20260529_01_feature_enhancement.sql
docker exec lab-management-mysql sh -lc "mysql --default-character-set=utf8mb4 -uroot -proot123456 lab_management_system < /tmp/V20260529_01_feature_enhancement.sql"
```

该增量已包含：
- `sys_audit_log` 审计日志表
- `v_equipment_borrow_status` 视图
- `sp_consumable_yearly_stats` 存储过程
- `trg_after_borrow_return` 触发器
- 库存预警、操作日志、到期提醒相关菜单与权限种子

## 编译验证

后端编译：

```powershell
cd .\backend
.\mvnw.cmd -q -DskipTests compile
```

前端构建：

```powershell
cd .\frontend
npm run build
```

## 项目文档

- 功能说明文档：`docs/feature_manual.md`
- 数据库设计文档：`docs/database_design.md`

## 注意事项

- 启动前请先确认 Docker Desktop 已运行
- 首次启动后端时 Maven 可能需要下载依赖
- 首次启动前端前请确认已执行 `npm install`
- 前端构建可能出现第三方依赖的 warning，不影响当前产物生成
