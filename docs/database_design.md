# 实验室设备与耗材管理系统数据库设计说明

## 1. 项目定位

本系统服务于高校实验室，支持实验室设备、试剂耗材、玻璃器皿、危化品的全生命周期管理，重点覆盖借用归还、校准维修、库存预警、成本核算、安全追踪与权限分级控制。

## 2. 角色与权限设计

### 2.1 系统管理员

- 核心身份：平台级超级管理员
- 使用目的：维护系统配置、账号体系、权限策略和审计日志
- 权限范围：
  - 管理全部用户、角色、菜单与权限映射
  - 查看全部实验室数据
  - 配置字典、编号规则、预警阈值、日志留存策略
  - 审计系统日志与关键业务日志

### 2.2 实验室主任

- 核心身份：实验室责任人
- 使用目的：统筹本实验室资产、安全与经费使用
- 权限范围：
  - 查看和审批本实验室设备借用、耗材申领、危化品领用
  - 查看本实验室统计报表和安全检查记录
  - 监督盘点、维修、校准、报废流程

### 2.3 设备管理员

- 核心身份：设备资产维护责任人
- 使用目的：维护设备全生命周期信息
- 权限范围：
  - 维护设备档案、类别、状态、存放信息
  - 处理设备借用归还
  - 维护校准计划、维修记录、报废记录
  - 查看设备使用率、故障统计

### 2.4 耗材管理员

- 核心身份：一般耗材与玻璃器皿库存责任人
- 使用目的：控制耗材库存和发放
- 权限范围：
  - 维护耗材档案与类别
  - 办理入库、出库、申领、盘点、损耗登记
  - 处理过期提醒和安全库存预警
  - 统计耗材消耗和金额

### 2.5 危化品管理员

- 核心身份：危化品专管人员
- 使用目的：严格管理危化品台账与安全流转
- 权限范围：
  - 维护危化品档案、批次、MSDS 关联信息
  - 办理危化品入库、领用、归还、退库、废液处理
  - 记录安全检查、双人领用、使用用途、余量回收
  - 查看危化品库存、过期、异常损耗与审计记录

### 2.6 普通教师

- 核心身份：实验课程和科研活动发起人
- 使用目的：申请借用设备、申领耗材、使用危化品
- 权限范围：
  - 提交设备借用申请
  - 提交耗材申领申请
  - 提交危化品领用申请
  - 查看本人记录和审批状态

### 2.7 学生

- 核心身份：实验操作执行人
- 使用目的：在授权范围内使用实验资源
- 权限范围：
  - 提交低风险设备借用申请
  - 提交低价值耗材申领申请
  - 查看本人借用、申领、违规与培训状态
  - 危化品操作须绑定指导教师或实验室授权

### 2.8 维修人员

- 核心身份：设备维修执行人
- 使用目的：处理设备报修、维修与回执
- 权限范围：
  - 接收维修任务
  - 更新维修状态、维修内容、维修费用、停机时间
  - 填写维修结果与恢复建议

### 2.9 校准人员

- 核心身份：计量校准执行人
- 使用目的：执行设备校准与结果登记
- 权限范围：
  - 查看待校准设备
  - 登记校准日期、结果、有效期、证书编号
  - 标记设备是否允许继续使用

## 3. 功能模块设计

### 3.1 用户与权限管理模块

- 用户档案维护：账号、工号学号、手机号、邮箱、所属实验室
- 角色管理：角色增删改查、状态控制
- RBAC 权限控制：用户与角色多对多映射
- 认证支持：供后续 Spring Security + JWT 接入
- 审计支持：关键操作写入系统日志

### 3.2 实验室基础信息管理模块

- 实验室档案：名称、编码、负责人、地点、电话、安全等级
- 负责人绑定：实验室与用户关联
- 状态管理：启用、停用、整改

### 3.3 设备管理模块

- 设备档案：编号、名称、型号、品牌、采购信息、价值、责任人
- 设备分类：分析仪器、通用设备、玻璃器皿等
- 借用归还：申请、审批、借出、归还、逾期管理
- 校准管理：计划、执行、结果、有效期
- 维修管理：报修、维修、验收、费用
- 报废管理：报废申请、审批、原因、处置方式

### 3.4 耗材管理模块

- 耗材档案：名称、规格、单位、类别、安全库存
- 入库管理：采购入库、调拨入库、盘盈入库
- 申领出库：教学、科研、维护等用途
- 盘点管理：盘盈、盘亏、损耗处理
- 过期提醒：批次有效期监控

### 3.5 危化品管理模块

- 危化品档案：CAS 编号、危险类别、储存要求、MSDS 编号
- 危化品入库：批次、浓度、纯度、包装规格
- 危化品领用：审批、双人登记、用途、实验项目
- 归还管理：余量归还、包装回收、状态核验
- 废液处理：废液类别、重量体积、移交去向
- 安全检查：巡检、温湿度、台账一致性

### 3.6 库存管理模块

- 实时库存：统一库存快照
- 预警管理：安全库存、过期预警、异常损耗预警
- 批次管理：适用于耗材和危化品
- 损耗统计：盘亏、蒸发、破损、污染等损耗原因

### 3.7 统计报表模块

- 设备使用率统计
- 设备故障和维修成本统计
- 耗材年度消耗统计
- 实验室成本核算
- 危化品领用汇总与余量回收统计

### 3.8 系统设置与日志模块

- 数据字典与系统参数配置
- 编码规则配置
- 系统日志、业务日志、状态变更日志
- 预警规则和消息开关

## 4. 核心业务流程

### 4.1 设备借用与归还流程

- 起点：教师或学生提交设备借用申请
- 关键环节：
  - 借用人填写设备、用途、借用时间、计划归还时间
  - 设备管理员检查设备状态是否为可用
  - 实验室主任按权限审批高价值或高风险设备
  - 审批通过后生成借用记录并更新设备状态为借出
  - 归还时管理员核验外观、配件、使用时长与完好状态
  - 若异常则转维修流程；若正常则更新归还时间并恢复可用
- 终点：借用记录归档，设备状态更新完成

### 4.2 耗材申领与出库流程

- 起点：教师或学生提交耗材申领单
- 关键环节：
  - 填写耗材、规格、数量、用途、项目归属
  - 耗材管理员检查库存和安全库存阈值
  - 如库存充足则审批出库并扣减库存
  - 如库存不足则进入补货提醒或部分出库流程
  - 出库完成后记录金额、操作人和剩余库存
- 终点：申领单完成，库存与统计同步更新

### 4.3 设备故障报修与维修流程

- 起点：借用人或管理员提交故障报修
- 关键环节：
  - 登记故障描述、故障时间、紧急程度
  - 设备管理员确认并锁定设备状态为维修中
  - 分派维修人员
  - 维修人员记录处理过程、维修费用、更换配件、恢复时间
  - 验收通过后设备状态恢复可用，否则继续停用或报废评估
- 终点：维修记录归档，设备状态最终确定

### 4.4 危化品领用与归还流程

- 起点：教师发起危化品领用申请，学生使用需绑定指导教师
- 关键环节：
  - 填写危化品、批次、用途、实验项目、计划用量
  - 危化品管理员核对库存、资质、安全培训状态
  - 审批通过后登记领出数量、领用人、复核人、容器编号
  - 使用结束后登记余量归还或废液处理
  - 系统更新批次库存并保留全流程痕迹
- 终点：危化品使用记录闭环，库存和安全台账一致

### 4.5 库存盘点流程

- 起点：管理员发起月度或学期盘点
- 关键环节：
  - 选定实验室、物资范围和盘点时间
  - 形成系统库存基线
  - 现场盘点并录入实盘数量
  - 对盘盈盘亏进行原因登记
  - 审核后更新库存，并产生损耗统计
- 终点：盘点结果生效，报表和预警同步更新

## 5. 概念模型设计

### 5.1 Chen 模型实体

- User
- Role
- Laboratory
- Equipment
- EquipmentCategory
- Consumable
- ConsumableCategory
- HazardousMaterial
- EquipmentBorrow
- EquipmentRepair
- EquipmentCalibration
- ConsumableInbound
- ConsumableOutbound
- HazardousMaterialUsage
- Inventory
- SystemLog

### 5.2 主要联系与基数

- User 与 Role：M:N
- Laboratory 与 User：1:N
- Laboratory 与 Equipment：1:N
- Laboratory 与 Consumable：1:N
- Laboratory 与 HazardousMaterial：1:N
- EquipmentCategory 与 Equipment：1:N
- ConsumableCategory 与 Consumable：1:N
- Equipment 与 EquipmentBorrow：1:N
- Equipment 与 EquipmentRepair：1:N
- Equipment 与 EquipmentCalibration：1:N
- Consumable 与 ConsumableInbound：1:N
- Consumable 与 ConsumableOutbound：1:N
- HazardousMaterial 与 HazardousMaterialUsage：1:N
- User 与 EquipmentBorrow：1:N
- User 与 ConsumableOutbound：1:N
- User 与 HazardousMaterialUsage：1:N
- User 与 SystemLog：1:N
- Laboratory 与 Inventory：1:N
- Consumable 与 Inventory：1:N
- HazardousMaterial 与 Inventory：1:N

### 5.3 各实体属性

#### User

- id
- laboratory_id
- username
- password_hash
- real_name
- user_no
- gender
- phone
- email
- user_type
- status
- last_login_time
- create_time
- update_time
- is_deleted

#### Role

- id
- role_code
- role_name
- description
- status
- create_time
- update_time
- is_deleted

#### Laboratory

- id
- director_user_id
- laboratory_code
- laboratory_name
- location
- contact_phone
- safety_level
- status
- create_time
- update_time
- is_deleted

#### EquipmentCategory

- id
- category_code
- category_name
- description
- status
- create_time
- update_time
- is_deleted

#### Equipment

- id
- laboratory_id
- category_id
- manager_user_id
- equipment_code
- equipment_name
- model
- brand
- manufacturer
- purchase_date
- purchase_price
- service_life_years
- storage_location
- status
- calibration_cycle_days
- last_calibration_date
- next_calibration_date
- remarks
- create_time
- update_time
- is_deleted

#### ConsumableCategory

- id
- category_code
- category_name
- description
- status
- create_time
- update_time
- is_deleted

#### Consumable

- id
- laboratory_id
- category_id
- manager_user_id
- consumable_code
- consumable_name
- specification
- unit
- unit_price
- safety_stock
- max_stock
- storage_location
- expiry_required
- status
- remarks
- create_time
- update_time
- is_deleted

#### HazardousMaterial

- id
- laboratory_id
- manager_user_id
- hazardous_code
- material_name
- cas_no
- hazard_category
- specification
- unit
- concentration
- storage_location
- safety_stock
- msds_code
- status
- remarks
- create_time
- update_time
- is_deleted

#### EquipmentBorrow

- id
- equipment_id
- laboratory_id
- borrower_user_id
- approver_user_id
- purpose
- borrow_date
- due_date
- actual_return_date
- borrow_status
- return_condition
- remarks
- create_time
- update_time
- is_deleted

#### EquipmentRepair

- id
- equipment_id
- laboratory_id
- reporter_user_id
- repair_user_id
- fault_description
- report_time
- repair_start_time
- repair_end_time
- repair_status
- repair_cost
- repair_result
- remarks
- create_time
- update_time
- is_deleted

#### EquipmentCalibration

- id
- equipment_id
- laboratory_id
- calibration_user_id
- certificate_no
- calibration_date
- valid_until
- calibration_result
- calibration_status
- remarks
- create_time
- update_time
- is_deleted

#### ConsumableInbound

- id
- consumable_id
- laboratory_id
- operator_user_id
- batch_no
- inbound_type
- quantity
- unit_price
- total_amount
- production_date
- expiry_date
- supplier_name
- inbound_date
- remarks
- create_time
- update_time
- is_deleted

#### ConsumableOutbound

- id
- consumable_id
- laboratory_id
- applicant_user_id
- approver_user_id
- operator_user_id
- batch_no
- outbound_type
- quantity
- unit_price
- total_amount
- outbound_date
- purpose
- outbound_status
- remarks
- create_time
- update_time
- is_deleted

#### HazardousMaterialUsage

- id
- hazardous_material_id
- laboratory_id
- applicant_user_id
- approver_user_id
- operator_user_id
- action_type
- batch_no
- quantity
- remaining_quantity
- usage_date
- purpose
- project_name
- witness_name
- usage_status
- remarks
- create_time
- update_time
- is_deleted

#### Inventory

- id
- laboratory_id
- item_type
- item_id
- batch_no
- quantity
- locked_quantity
- unit_price
- production_date
- expiry_date
- last_stock_in_time
- last_stock_out_time
- warning_status
- remarks
- create_time
- update_time
- is_deleted

#### SystemLog

- id
- user_id
- laboratory_id
- module_name
- operation_type
- business_key
- operation_desc
- request_ip
- operation_status
- operation_time
- create_time
- update_time
- is_deleted

## 6. 逻辑设计原则

- 所有业务主表采用自增整数主键
- 所有实体表统一包含 `create_time`、`update_time`、`is_deleted`
- 所有状态字段使用 `TINYINT`
- 通过 `CHECK` 约束控制数量、金额、日期逻辑
- 唯一编码采用 `UNIQUE KEY`
- 全部外键设置 `ON UPDATE CASCADE`
- 删除策略遵循软删除，外键删除策略以 `RESTRICT` 或 `SET NULL` 为主，避免误删业务数据

## 7. 物理设计与索引策略

### 7.1 外键索引

- 为所有外键字段建立普通索引
- 原因：加速关联查询、减少外键检查成本

### 7.2 高频过滤索引

- `equipment.equipment_code`
  - 类型：唯一索引
  - 原因：设备按编号精确查询最频繁
- `equipment.status`
  - 类型：普通索引
  - 原因：常用于筛选可用、借出、维修状态
- `equipment_borrow.borrow_status`
  - 类型：普通索引
  - 原因：查询当前借出设备
- `equipment_borrow.borrow_date`
  - 类型：普通索引
  - 原因：按借用时间范围统计
- `consumable.consumable_code`
  - 类型：唯一索引
  - 原因：耗材主数据编码检索
- `consumable_outbound.outbound_date`
  - 类型：普通索引
  - 原因：年度与月度消耗统计
- `consumable_outbound.outbound_status`
  - 类型：普通索引
  - 原因：区分已出库、待审批
- `hazardous_material.hazardous_code`
  - 类型：唯一索引
  - 原因：危化品台账精准定位
- `hazardous_material_usage.usage_date`
  - 类型：普通索引
  - 原因：危化品按日期追溯
- `inventory.warning_status`
  - 类型：普通索引
  - 原因：快速查询库存预警
- `inventory.expiry_date`
  - 类型：普通索引
  - 原因：查询即将过期批次

### 7.3 复合索引

- `equipment_borrow (laboratory_id, borrow_status, due_date)`
  - 原因：查询实验室当前借出和逾期设备
- `consumable_outbound (consumable_id, outbound_date)`
  - 原因：按耗材和时间统计消耗
- `consumable_inbound (laboratory_id, inbound_date)`
  - 原因：按实验室查询近半年入库记录
- `hazardous_material_usage (hazardous_material_id, action_type, usage_date)`
  - 原因：危化品按动作类型追踪领用归还
- `inventory (item_type, item_id, batch_no)`
  - 原因：统一库存定位单个物资批次

## 8. 3NF 说明

- 实验室、角色、类别主数据独立成表，避免冗余
- 设备、耗材、危化品主数据与流水数据分离
- 借用、维修、校准、出入库全部独立建表
- 统计类信息不直接冗余存储在主表中，主要通过查询、视图和汇总生成

## 9. 约束策略说明

- 数量与单价必须大于等于 0
- 归还日期不能早于借用日期
- 校准有效期不能早于校准日期
- 维修结束时间不能早于维修开始时间
- 过期日期不能早于生产日期

## 10. 推荐后续工程扩展

- 对接 Spring Boot 3 + MyBatis-Plus
- 为库存预警接入 Redis 缓存和发布订阅
- 为审批流接入工作流引擎或简化状态机
- 为附件台账扩展文件表，存储 MSDS、校准证书、维修单图片
