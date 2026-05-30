-- 实验室设备与耗材管理系统数据库脚本
-- 适用数据库：MySQL 8.0+

DROP DATABASE IF EXISTS lab_management_system;
CREATE DATABASE lab_management_system
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE lab_management_system;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 角色表：存储系统角色定义
CREATE TABLE role (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '角色主键ID',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    role_name VARCHAR(100) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) NULL COMMENT '角色描述',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    CONSTRAINT uk_role_code UNIQUE (role_code),
    CONSTRAINT uk_role_name UNIQUE (role_name),
    CONSTRAINT chk_role_status CHECK (status IN (0, 1)),
    CONSTRAINT chk_role_deleted CHECK (is_deleted IN (0, 1))
) COMMENT = '角色表';

-- 实验室表：存储实验室基础信息
CREATE TABLE laboratory (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '实验室主键ID',
    director_user_id INT NULL COMMENT '实验室主任用户ID',
    laboratory_code VARCHAR(50) NOT NULL COMMENT '实验室编码',
    laboratory_name VARCHAR(100) NOT NULL COMMENT '实验室名称',
    location VARCHAR(200) NOT NULL COMMENT '实验室地点',
    contact_phone VARCHAR(30) NULL COMMENT '联系电话',
    safety_level TINYINT NOT NULL DEFAULT 1 COMMENT '安全等级：1-低，2-中，3-高',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-启用，2-整改',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    CONSTRAINT uk_laboratory_code UNIQUE (laboratory_code),
    CONSTRAINT uk_laboratory_name UNIQUE (laboratory_name),
    CONSTRAINT chk_laboratory_safety_level CHECK (safety_level IN (1, 2, 3)),
    CONSTRAINT chk_laboratory_status CHECK (status IN (0, 1, 2)),
    CONSTRAINT chk_laboratory_deleted CHECK (is_deleted IN (0, 1))
) COMMENT = '实验室表';

-- 用户表：存储平台用户信息
CREATE TABLE user (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户主键ID',
    laboratory_id INT NULL COMMENT '所属实验室ID',
    username VARCHAR(50) NOT NULL COMMENT '登录用户名',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    real_name VARCHAR(100) NOT NULL COMMENT '真实姓名',
    user_no VARCHAR(50) NOT NULL COMMENT '工号或学号',
    gender TINYINT NOT NULL DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    phone VARCHAR(30) NULL COMMENT '手机号',
    email VARCHAR(100) NULL COMMENT '邮箱',
    user_type TINYINT NOT NULL COMMENT '用户类型：1-系统管理员，2-实验室主任，3-设备管理员，4-耗材管理员，5-危化品管理员，6-教师，7-学生，8-维修人员，9-校准人员',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    last_login_time DATETIME NULL COMMENT '最后登录时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    CONSTRAINT uk_user_username UNIQUE (username),
    CONSTRAINT uk_user_user_no UNIQUE (user_no),
    CONSTRAINT uk_user_phone UNIQUE (phone),
    CONSTRAINT uk_user_email UNIQUE (email),
    CONSTRAINT chk_user_gender CHECK (gender IN (0, 1, 2)),
    CONSTRAINT chk_user_type CHECK (user_type BETWEEN 1 AND 9),
    CONSTRAINT chk_user_status CHECK (status IN (0, 1)),
    CONSTRAINT chk_user_deleted CHECK (is_deleted IN (0, 1)),
    CONSTRAINT fk_user_laboratory FOREIGN KEY (laboratory_id)
        REFERENCES laboratory (id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
) COMMENT = '用户表';

-- 用户角色关联表：存储用户与角色多对多关系
CREATE TABLE user_role (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户角色关系主键ID',
    user_id INT NOT NULL COMMENT '用户ID',
    role_id INT NOT NULL COMMENT '角色ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    CONSTRAINT uk_user_role UNIQUE (user_id, role_id),
    CONSTRAINT chk_user_role_deleted CHECK (is_deleted IN (0, 1)),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id)
        REFERENCES user (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id)
        REFERENCES role (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) COMMENT = '用户角色关联表';

-- 菜单表：存储前端菜单与权限点
CREATE TABLE menu (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '菜单主键ID',
    parent_id INT NULL COMMENT '父级菜单ID',
    menu_code VARCHAR(50) NOT NULL COMMENT '菜单编码',
    menu_name VARCHAR(100) NOT NULL COMMENT '菜单名称',
    route_path VARCHAR(200) NOT NULL COMMENT '前端路由路径',
    component_name VARCHAR(200) NULL COMMENT '前端组件名称',
    permission_code VARCHAR(100) NULL COMMENT '权限标识',
    menu_type TINYINT NOT NULL DEFAULT 1 COMMENT '菜单类型：1-菜单，2-按钮',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
    visible TINYINT NOT NULL DEFAULT 1 COMMENT '是否可见：0-否，1-是',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    icon VARCHAR(100) NULL COMMENT '图标',
    remarks VARCHAR(500) NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    CONSTRAINT uk_menu_code UNIQUE (menu_code),
    CONSTRAINT chk_menu_type CHECK (menu_type IN (1, 2)),
    CONSTRAINT chk_menu_visible CHECK (visible IN (0, 1)),
    CONSTRAINT chk_menu_status CHECK (status IN (0, 1)),
    CONSTRAINT chk_menu_deleted CHECK (is_deleted IN (0, 1)),
    CONSTRAINT fk_menu_parent FOREIGN KEY (parent_id)
        REFERENCES menu (id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
) COMMENT = '菜单表';

-- 角色菜单关联表：存储角色与菜单/权限点的关联关系
CREATE TABLE role_menu (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '角色菜单关系主键ID',
    role_id INT NOT NULL COMMENT '角色ID',
    menu_id INT NOT NULL COMMENT '菜单ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    CONSTRAINT uk_role_menu UNIQUE (role_id, menu_id),
    CONSTRAINT chk_role_menu_deleted CHECK (is_deleted IN (0, 1)),
    CONSTRAINT fk_role_menu_role FOREIGN KEY (role_id)
        REFERENCES role (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_role_menu_menu FOREIGN KEY (menu_id)
        REFERENCES menu (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) COMMENT = '角色菜单关联表';

-- 由于 laboratory 依赖 user，补充实验室主任外键
ALTER TABLE laboratory
    ADD CONSTRAINT fk_laboratory_director_user FOREIGN KEY (director_user_id)
        REFERENCES user (id)
        ON UPDATE CASCADE
        ON DELETE SET NULL;

-- 设备类别表：存储设备分类
CREATE TABLE equipment_category (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '设备类别主键ID',
    category_code VARCHAR(50) NOT NULL COMMENT '设备类别编码',
    category_name VARCHAR(100) NOT NULL COMMENT '设备类别名称',
    description VARCHAR(255) NULL COMMENT '类别描述',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    CONSTRAINT uk_equipment_category_code UNIQUE (category_code),
    CONSTRAINT uk_equipment_category_name UNIQUE (category_name),
    CONSTRAINT chk_equipment_category_status CHECK (status IN (0, 1)),
    CONSTRAINT chk_equipment_category_deleted CHECK (is_deleted IN (0, 1))
) COMMENT = '设备类别表';

-- 设备表：存储设备主数据
CREATE TABLE equipment (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '设备主键ID',
    laboratory_id INT NOT NULL COMMENT '所属实验室ID',
    category_id INT NOT NULL COMMENT '设备类别ID',
    manager_user_id INT NULL COMMENT '设备管理员用户ID',
    equipment_code VARCHAR(50) NOT NULL COMMENT '设备编号',
    equipment_name VARCHAR(100) NOT NULL COMMENT '设备名称',
    model VARCHAR(100) NOT NULL COMMENT '型号',
    brand VARCHAR(100) NULL COMMENT '品牌',
    manufacturer VARCHAR(100) NULL COMMENT '生产厂家',
    purchase_date DATETIME NOT NULL COMMENT '购置日期',
    purchase_price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '购置金额',
    service_life_years INT NOT NULL DEFAULT 5 COMMENT '使用年限',
    storage_location VARCHAR(200) NOT NULL COMMENT '存放位置',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-可用，2-借出，3-维修中，4-校准中，5-报废',
    calibration_cycle_days INT NOT NULL DEFAULT 365 COMMENT '校准周期天数',
    last_calibration_date DATETIME NULL COMMENT '上次校准日期',
    next_calibration_date DATETIME NULL COMMENT '下次校准日期',
    remarks VARCHAR(500) NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    CONSTRAINT uk_equipment_code UNIQUE (equipment_code),
    CONSTRAINT chk_equipment_price CHECK (purchase_price >= 0),
    CONSTRAINT chk_equipment_service_life CHECK (service_life_years > 0),
    CONSTRAINT chk_equipment_status CHECK (status IN (0, 1, 2, 3, 4, 5)),
    CONSTRAINT chk_equipment_cycle CHECK (calibration_cycle_days > 0),
    CONSTRAINT chk_equipment_deleted CHECK (is_deleted IN (0, 1)),
    CONSTRAINT fk_equipment_laboratory FOREIGN KEY (laboratory_id)
        REFERENCES laboratory (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_equipment_category FOREIGN KEY (category_id)
        REFERENCES equipment_category (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_equipment_manager_user FOREIGN KEY (manager_user_id)
        REFERENCES user (id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
) COMMENT = '设备表';

-- 耗材类别表：存储耗材分类
CREATE TABLE consumable_category (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '耗材类别主键ID',
    category_code VARCHAR(50) NOT NULL COMMENT '耗材类别编码',
    category_name VARCHAR(100) NOT NULL COMMENT '耗材类别名称',
    description VARCHAR(255) NULL COMMENT '类别描述',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    CONSTRAINT uk_consumable_category_code UNIQUE (category_code),
    CONSTRAINT uk_consumable_category_name UNIQUE (category_name),
    CONSTRAINT chk_consumable_category_status CHECK (status IN (0, 1)),
    CONSTRAINT chk_consumable_category_deleted CHECK (is_deleted IN (0, 1))
) COMMENT = '耗材类别表';

-- 耗材表：存储耗材主数据
CREATE TABLE consumable (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '耗材主键ID',
    laboratory_id INT NOT NULL COMMENT '所属实验室ID',
    category_id INT NOT NULL COMMENT '耗材类别ID',
    manager_user_id INT NULL COMMENT '耗材管理员用户ID',
    consumable_code VARCHAR(50) NOT NULL COMMENT '耗材编号',
    consumable_name VARCHAR(100) NOT NULL COMMENT '耗材名称',
    specification VARCHAR(100) NOT NULL COMMENT '规格型号',
    unit VARCHAR(20) NOT NULL COMMENT '计量单位',
    unit_price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '参考单价',
    safety_stock DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '安全库存',
    max_stock DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '最大库存',
    storage_location VARCHAR(200) NOT NULL COMMENT '存放位置',
    expiry_required TINYINT NOT NULL DEFAULT 1 COMMENT '是否管理有效期：0-否，1-是',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-启用',
    remarks VARCHAR(500) NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    CONSTRAINT uk_consumable_code UNIQUE (consumable_code),
    CONSTRAINT chk_consumable_price CHECK (unit_price >= 0),
    CONSTRAINT chk_consumable_safety_stock CHECK (safety_stock >= 0),
    CONSTRAINT chk_consumable_max_stock CHECK (max_stock >= 0),
    CONSTRAINT chk_consumable_expiry_required CHECK (expiry_required IN (0, 1)),
    CONSTRAINT chk_consumable_status CHECK (status IN (0, 1)),
    CONSTRAINT chk_consumable_deleted CHECK (is_deleted IN (0, 1)),
    CONSTRAINT fk_consumable_laboratory FOREIGN KEY (laboratory_id)
        REFERENCES laboratory (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_consumable_category FOREIGN KEY (category_id)
        REFERENCES consumable_category (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_consumable_manager_user FOREIGN KEY (manager_user_id)
        REFERENCES user (id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
) COMMENT = '耗材表';

-- 危化品表：存储危化品主数据
CREATE TABLE hazardous_material (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '危化品主键ID',
    laboratory_id INT NOT NULL COMMENT '所属实验室ID',
    manager_user_id INT NULL COMMENT '危化品管理员用户ID',
    hazardous_code VARCHAR(50) NOT NULL COMMENT '危化品编号',
    material_name VARCHAR(100) NOT NULL COMMENT '危化品名称',
    cas_no VARCHAR(50) NULL COMMENT 'CAS编号',
    hazard_category VARCHAR(100) NOT NULL COMMENT '危险类别',
    specification VARCHAR(100) NOT NULL COMMENT '规格型号',
    unit VARCHAR(20) NOT NULL COMMENT '计量单位',
    concentration VARCHAR(100) NULL COMMENT '浓度或纯度',
    storage_location VARCHAR(200) NOT NULL COMMENT '存放位置',
    safety_stock DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '安全库存',
    msds_code VARCHAR(100) NULL COMMENT 'MSDS编号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-启用，2-受控',
    remarks VARCHAR(500) NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    CONSTRAINT uk_hazardous_code UNIQUE (hazardous_code),
    CONSTRAINT chk_hazardous_safety_stock CHECK (safety_stock >= 0),
    CONSTRAINT chk_hazardous_status CHECK (status IN (0, 1, 2)),
    CONSTRAINT chk_hazardous_deleted CHECK (is_deleted IN (0, 1)),
    CONSTRAINT fk_hazardous_laboratory FOREIGN KEY (laboratory_id)
        REFERENCES laboratory (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_hazardous_manager_user FOREIGN KEY (manager_user_id)
        REFERENCES user (id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
) COMMENT = '危化品表';

-- 设备借用记录表：存储设备借还信息
CREATE TABLE equipment_borrow (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '设备借用记录主键ID',
    equipment_id INT NOT NULL COMMENT '设备ID',
    laboratory_id INT NOT NULL COMMENT '实验室ID',
    borrower_user_id INT NOT NULL COMMENT '借用人ID',
    approver_user_id INT NULL COMMENT '审批人ID',
    purpose VARCHAR(255) NOT NULL COMMENT '借用用途',
    borrow_date DATETIME NOT NULL COMMENT '借用日期',
    due_date DATETIME NOT NULL COMMENT '应归还日期',
    actual_return_date DATETIME NULL COMMENT '实际归还日期',
    borrow_status TINYINT NOT NULL DEFAULT 1 COMMENT '借用状态：1-待审批，2-已借出，3-已归还，4-已逾期，5-已拒绝',
    return_condition VARCHAR(255) NULL COMMENT '归还情况',
    remarks VARCHAR(500) NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    CONSTRAINT chk_equipment_borrow_date CHECK (due_date >= borrow_date),
    CONSTRAINT chk_equipment_borrow_return_date CHECK (actual_return_date IS NULL OR actual_return_date >= borrow_date),
    CONSTRAINT chk_equipment_borrow_status CHECK (borrow_status IN (1, 2, 3, 4, 5)),
    CONSTRAINT chk_equipment_borrow_deleted CHECK (is_deleted IN (0, 1)),
    CONSTRAINT fk_equipment_borrow_equipment FOREIGN KEY (equipment_id)
        REFERENCES equipment (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_equipment_borrow_laboratory FOREIGN KEY (laboratory_id)
        REFERENCES laboratory (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_equipment_borrow_borrower FOREIGN KEY (borrower_user_id)
        REFERENCES user (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_equipment_borrow_approver FOREIGN KEY (approver_user_id)
        REFERENCES user (id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
) COMMENT = '设备借用记录表';

-- 设备维修记录表：存储设备维修过程
CREATE TABLE equipment_repair (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '设备维修记录主键ID',
    equipment_id INT NOT NULL COMMENT '设备ID',
    laboratory_id INT NOT NULL COMMENT '实验室ID',
    reporter_user_id INT NOT NULL COMMENT '报修人ID',
    repair_user_id INT NULL COMMENT '维修人员ID',
    fault_description VARCHAR(500) NOT NULL COMMENT '故障描述',
    report_time DATETIME NOT NULL COMMENT '报修时间',
    repair_start_time DATETIME NULL COMMENT '维修开始时间',
    repair_end_time DATETIME NULL COMMENT '维修结束时间',
    repair_status TINYINT NOT NULL DEFAULT 1 COMMENT '维修状态：1-待受理，2-维修中，3-已完成，4-无法修复',
    repair_cost DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '维修费用',
    repair_result VARCHAR(500) NULL COMMENT '维修结果',
    remarks VARCHAR(500) NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    CONSTRAINT chk_equipment_repair_time CHECK (
        repair_start_time IS NULL
        OR repair_end_time IS NULL
        OR repair_end_time >= repair_start_time
    ),
    CONSTRAINT chk_equipment_repair_cost CHECK (repair_cost >= 0),
    CONSTRAINT chk_equipment_repair_status CHECK (repair_status IN (1, 2, 3, 4)),
    CONSTRAINT chk_equipment_repair_deleted CHECK (is_deleted IN (0, 1)),
    CONSTRAINT fk_equipment_repair_equipment FOREIGN KEY (equipment_id)
        REFERENCES equipment (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_equipment_repair_laboratory FOREIGN KEY (laboratory_id)
        REFERENCES laboratory (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_equipment_repair_reporter FOREIGN KEY (reporter_user_id)
        REFERENCES user (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_equipment_repair_repair_user FOREIGN KEY (repair_user_id)
        REFERENCES user (id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
) COMMENT = '设备维修记录表';

-- 设备校准记录表：存储设备校准信息
CREATE TABLE equipment_calibration (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '设备校准记录主键ID',
    equipment_id INT NOT NULL COMMENT '设备ID',
    laboratory_id INT NOT NULL COMMENT '实验室ID',
    calibration_user_id INT NULL COMMENT '校准人员ID',
    certificate_no VARCHAR(100) NOT NULL COMMENT '校准证书编号',
    calibration_date DATETIME NOT NULL COMMENT '校准日期',
    valid_until DATETIME NOT NULL COMMENT '有效期截止日期',
    calibration_result TINYINT NOT NULL COMMENT '校准结果：1-合格，2-不合格',
    calibration_status TINYINT NOT NULL DEFAULT 1 COMMENT '记录状态：1-待确认，2-已完成',
    remarks VARCHAR(500) NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    CONSTRAINT uk_equipment_calibration_certificate UNIQUE (certificate_no),
    CONSTRAINT chk_equipment_calibration_valid CHECK (valid_until >= calibration_date),
    CONSTRAINT chk_equipment_calibration_result CHECK (calibration_result IN (1, 2)),
    CONSTRAINT chk_equipment_calibration_status CHECK (calibration_status IN (1, 2)),
    CONSTRAINT chk_equipment_calibration_deleted CHECK (is_deleted IN (0, 1)),
    CONSTRAINT fk_equipment_calibration_equipment FOREIGN KEY (equipment_id)
        REFERENCES equipment (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_equipment_calibration_laboratory FOREIGN KEY (laboratory_id)
        REFERENCES laboratory (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_equipment_calibration_user FOREIGN KEY (calibration_user_id)
        REFERENCES user (id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
) COMMENT = '设备校准记录表';

-- 耗材入库记录表：存储耗材入库流水
CREATE TABLE consumable_inbound (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '耗材入库记录主键ID',
    consumable_id INT NOT NULL COMMENT '耗材ID',
    laboratory_id INT NOT NULL COMMENT '实验室ID',
    operator_user_id INT NOT NULL COMMENT '操作人ID',
    batch_no VARCHAR(100) NOT NULL COMMENT '批号',
    inbound_type TINYINT NOT NULL DEFAULT 1 COMMENT '入库类型：1-采购入库，2-调拨入库，3-盘盈入库',
    quantity DECIMAL(10,2) NOT NULL COMMENT '入库数量',
    unit_price DECIMAL(10,2) NOT NULL COMMENT '入库单价',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '入库总金额',
    production_date DATETIME NULL COMMENT '生产日期',
    expiry_date DATETIME NULL COMMENT '过期日期',
    supplier_name VARCHAR(200) NULL COMMENT '供应商名称',
    inbound_date DATETIME NOT NULL COMMENT '入库日期',
    remarks VARCHAR(500) NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    CONSTRAINT chk_consumable_inbound_quantity CHECK (quantity > 0),
    CONSTRAINT chk_consumable_inbound_unit_price CHECK (unit_price >= 0),
    CONSTRAINT chk_consumable_inbound_amount CHECK (total_amount >= 0),
    CONSTRAINT chk_consumable_inbound_type CHECK (inbound_type IN (1, 2, 3)),
    CONSTRAINT chk_consumable_inbound_expiry CHECK (expiry_date IS NULL OR production_date IS NULL OR expiry_date >= production_date),
    CONSTRAINT chk_consumable_inbound_deleted CHECK (is_deleted IN (0, 1)),
    CONSTRAINT fk_consumable_inbound_consumable FOREIGN KEY (consumable_id)
        REFERENCES consumable (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_consumable_inbound_laboratory FOREIGN KEY (laboratory_id)
        REFERENCES laboratory (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_consumable_inbound_operator FOREIGN KEY (operator_user_id)
        REFERENCES user (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) COMMENT = '耗材入库记录表';

-- 耗材出库记录表：存储耗材申领与出库流水
CREATE TABLE consumable_outbound (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '耗材出库记录主键ID',
    consumable_id INT NOT NULL COMMENT '耗材ID',
    laboratory_id INT NOT NULL COMMENT '实验室ID',
    applicant_user_id INT NOT NULL COMMENT '申请人ID',
    approver_user_id INT NULL COMMENT '审批人ID',
    operator_user_id INT NULL COMMENT '出库操作人ID',
    batch_no VARCHAR(100) NOT NULL COMMENT '批号',
    outbound_type TINYINT NOT NULL DEFAULT 1 COMMENT '出库类型：1-教学领用，2-科研领用，3-维护领用，4-报损出库',
    quantity DECIMAL(10,2) NOT NULL COMMENT '出库数量',
    unit_price DECIMAL(10,2) NOT NULL COMMENT '出库单价',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '出库总金额',
    outbound_date DATETIME NOT NULL COMMENT '出库日期',
    purpose VARCHAR(255) NOT NULL COMMENT '出库用途',
    outbound_status TINYINT NOT NULL DEFAULT 1 COMMENT '出库状态：1-待审批，2-已出库，3-已拒绝',
    remarks VARCHAR(500) NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    CONSTRAINT chk_consumable_outbound_quantity CHECK (quantity > 0),
    CONSTRAINT chk_consumable_outbound_unit_price CHECK (unit_price >= 0),
    CONSTRAINT chk_consumable_outbound_amount CHECK (total_amount >= 0),
    CONSTRAINT chk_consumable_outbound_type CHECK (outbound_type IN (1, 2, 3, 4)),
    CONSTRAINT chk_consumable_outbound_status CHECK (outbound_status IN (1, 2, 3)),
    CONSTRAINT chk_consumable_outbound_deleted CHECK (is_deleted IN (0, 1)),
    CONSTRAINT fk_consumable_outbound_consumable FOREIGN KEY (consumable_id)
        REFERENCES consumable (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_consumable_outbound_laboratory FOREIGN KEY (laboratory_id)
        REFERENCES laboratory (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_consumable_outbound_applicant FOREIGN KEY (applicant_user_id)
        REFERENCES user (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_consumable_outbound_approver FOREIGN KEY (approver_user_id)
        REFERENCES user (id)
        ON UPDATE CASCADE
        ON DELETE SET NULL,
    CONSTRAINT fk_consumable_outbound_operator FOREIGN KEY (operator_user_id)
        REFERENCES user (id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
) COMMENT = '耗材出库记录表';

-- 危化品使用记录表：统一记录危化品入库、领用、归还、废液处理
CREATE TABLE hazardous_material_usage (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '危化品使用记录主键ID',
    hazardous_material_id INT NOT NULL COMMENT '危化品ID',
    laboratory_id INT NOT NULL COMMENT '实验室ID',
    applicant_user_id INT NOT NULL COMMENT '申请人ID',
    approver_user_id INT NULL COMMENT '审批人ID',
    operator_user_id INT NULL COMMENT '操作人ID',
    action_type TINYINT NOT NULL COMMENT '动作类型：1-入库，2-领用，3-归还，4-废液处理',
    batch_no VARCHAR(100) NOT NULL COMMENT '批号',
    quantity DECIMAL(10,2) NOT NULL COMMENT '发生数量',
    remaining_quantity DECIMAL(10,2) NULL COMMENT '余量数量',
    usage_date DATETIME NOT NULL COMMENT '业务日期',
    purpose VARCHAR(255) NULL COMMENT '用途',
    project_name VARCHAR(255) NULL COMMENT '项目名称',
    witness_name VARCHAR(100) NULL COMMENT '复核人姓名',
    usage_status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-待审批，2-已完成，3-已拒绝',
    remarks VARCHAR(500) NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    CONSTRAINT chk_hazardous_usage_quantity CHECK (quantity > 0),
    CONSTRAINT chk_hazardous_usage_remaining CHECK (remaining_quantity IS NULL OR remaining_quantity >= 0),
    CONSTRAINT chk_hazardous_usage_action CHECK (action_type IN (1, 2, 3, 4)),
    CONSTRAINT chk_hazardous_usage_status CHECK (usage_status IN (1, 2, 3)),
    CONSTRAINT chk_hazardous_usage_deleted CHECK (is_deleted IN (0, 1)),
    CONSTRAINT fk_hazardous_usage_material FOREIGN KEY (hazardous_material_id)
        REFERENCES hazardous_material (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_hazardous_usage_laboratory FOREIGN KEY (laboratory_id)
        REFERENCES laboratory (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_hazardous_usage_applicant FOREIGN KEY (applicant_user_id)
        REFERENCES user (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_hazardous_usage_approver FOREIGN KEY (approver_user_id)
        REFERENCES user (id)
        ON UPDATE CASCADE
        ON DELETE SET NULL,
    CONSTRAINT fk_hazardous_usage_operator FOREIGN KEY (operator_user_id)
        REFERENCES user (id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
) COMMENT = '危化品使用记录表';

-- 库存记录表：统一记录耗材与危化品批次库存
CREATE TABLE inventory (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '库存记录主键ID',
    laboratory_id INT NOT NULL COMMENT '实验室ID',
    item_type TINYINT NOT NULL COMMENT '物品类型：1-设备，2-耗材，3-危化品',
    item_id INT NOT NULL COMMENT '物品ID',
    batch_no VARCHAR(100) NOT NULL COMMENT '批号',
    quantity DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '当前库存数量',
    locked_quantity DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '锁定数量',
    unit_price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '单价',
    production_date DATETIME NULL COMMENT '生产日期',
    expiry_date DATETIME NULL COMMENT '过期日期',
    last_stock_in_time DATETIME NULL COMMENT '最近入库时间',
    last_stock_out_time DATETIME NULL COMMENT '最近出库时间',
    warning_status TINYINT NOT NULL DEFAULT 0 COMMENT '预警状态：0-正常，1-低库存，2-即将过期，3-已过期',
    remarks VARCHAR(500) NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    CONSTRAINT uk_inventory_item_batch UNIQUE (laboratory_id, item_type, item_id, batch_no),
    CONSTRAINT chk_inventory_item_type CHECK (item_type IN (1, 2, 3)),
    CONSTRAINT chk_inventory_quantity CHECK (quantity >= 0),
    CONSTRAINT chk_inventory_locked_quantity CHECK (locked_quantity >= 0),
    CONSTRAINT chk_inventory_unit_price CHECK (unit_price >= 0),
    CONSTRAINT chk_inventory_warning_status CHECK (warning_status IN (0, 1, 2, 3)),
    CONSTRAINT chk_inventory_expiry CHECK (expiry_date IS NULL OR production_date IS NULL OR expiry_date >= production_date),
    CONSTRAINT chk_inventory_deleted CHECK (is_deleted IN (0, 1)),
    CONSTRAINT fk_inventory_laboratory FOREIGN KEY (laboratory_id)
        REFERENCES laboratory (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) COMMENT = '库存记录表';

-- 系统日志表：记录关键操作日志
CREATE TABLE system_log (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '系统日志主键ID',
    user_id INT NULL COMMENT '操作用户ID',
    laboratory_id INT NULL COMMENT '实验室ID',
    module_name VARCHAR(100) NOT NULL COMMENT '模块名称',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    business_key VARCHAR(100) NULL COMMENT '业务主键',
    operation_desc VARCHAR(500) NOT NULL COMMENT '操作描述',
    request_ip VARCHAR(50) NULL COMMENT '请求IP',
    operation_status TINYINT NOT NULL DEFAULT 1 COMMENT '操作状态：0-失败，1-成功',
    operation_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    CONSTRAINT chk_system_log_status CHECK (operation_status IN (0, 1)),
    CONSTRAINT chk_system_log_deleted CHECK (is_deleted IN (0, 1)),
    CONSTRAINT fk_system_log_user FOREIGN KEY (user_id)
        REFERENCES user (id)
        ON UPDATE CASCADE
        ON DELETE SET NULL,
    CONSTRAINT fk_system_log_laboratory FOREIGN KEY (laboratory_id)
        REFERENCES laboratory (id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
) COMMENT = '系统日志表';

SET FOREIGN_KEY_CHECKS = 1;

-- 外键索引：加速关联查询和外键检查
CREATE INDEX idx_user_laboratory_id ON user (laboratory_id);
CREATE INDEX idx_user_user_type ON user (user_type);
CREATE INDEX idx_user_status ON user (status);

CREATE INDEX idx_user_role_user_id ON user_role (user_id);
CREATE INDEX idx_user_role_role_id ON user_role (role_id);
CREATE INDEX idx_menu_parent_id ON menu (parent_id);
CREATE INDEX idx_menu_status_visible_sort ON menu (status, visible, sort_order);
CREATE INDEX idx_role_menu_role_id ON role_menu (role_id);
CREATE INDEX idx_role_menu_menu_id ON role_menu (menu_id);

CREATE INDEX idx_laboratory_director_user_id ON laboratory (director_user_id);
CREATE INDEX idx_laboratory_status ON laboratory (status);

CREATE INDEX idx_equipment_laboratory_id ON equipment (laboratory_id);
CREATE INDEX idx_equipment_category_id ON equipment (category_id);
CREATE INDEX idx_equipment_manager_user_id ON equipment (manager_user_id);
CREATE INDEX idx_equipment_status ON equipment (status);
CREATE INDEX idx_equipment_purchase_date ON equipment (purchase_date);
CREATE INDEX idx_equipment_next_calibration_date ON equipment (next_calibration_date);

CREATE INDEX idx_consumable_laboratory_id ON consumable (laboratory_id);
CREATE INDEX idx_consumable_category_id ON consumable (category_id);
CREATE INDEX idx_consumable_manager_user_id ON consumable (manager_user_id);
CREATE INDEX idx_consumable_status ON consumable (status);

CREATE INDEX idx_hazardous_laboratory_id ON hazardous_material (laboratory_id);
CREATE INDEX idx_hazardous_manager_user_id ON hazardous_material (manager_user_id);
CREATE INDEX idx_hazardous_status ON hazardous_material (status);

CREATE INDEX idx_equipment_borrow_equipment_id ON equipment_borrow (equipment_id);
CREATE INDEX idx_equipment_borrow_laboratory_id ON equipment_borrow (laboratory_id);
CREATE INDEX idx_equipment_borrow_borrower_user_id ON equipment_borrow (borrower_user_id);
CREATE INDEX idx_equipment_borrow_approver_user_id ON equipment_borrow (approver_user_id);
CREATE INDEX idx_equipment_borrow_status ON equipment_borrow (borrow_status);
CREATE INDEX idx_equipment_borrow_date ON equipment_borrow (borrow_date);
CREATE INDEX idx_equipment_borrow_due_date ON equipment_borrow (due_date);
CREATE INDEX idx_equipment_borrow_lab_status_due ON equipment_borrow (laboratory_id, borrow_status, due_date);

CREATE INDEX idx_equipment_repair_equipment_id ON equipment_repair (equipment_id);
CREATE INDEX idx_equipment_repair_laboratory_id ON equipment_repair (laboratory_id);
CREATE INDEX idx_equipment_repair_reporter_user_id ON equipment_repair (reporter_user_id);
CREATE INDEX idx_equipment_repair_repair_user_id ON equipment_repair (repair_user_id);
CREATE INDEX idx_equipment_repair_status ON equipment_repair (repair_status);
CREATE INDEX idx_equipment_repair_report_time ON equipment_repair (report_time);

CREATE INDEX idx_equipment_calibration_equipment_id ON equipment_calibration (equipment_id);
CREATE INDEX idx_equipment_calibration_laboratory_id ON equipment_calibration (laboratory_id);
CREATE INDEX idx_equipment_calibration_user_id ON equipment_calibration (calibration_user_id);
CREATE INDEX idx_equipment_calibration_date ON equipment_calibration (calibration_date);
CREATE INDEX idx_equipment_calibration_valid_until ON equipment_calibration (valid_until);

CREATE INDEX idx_consumable_inbound_consumable_id ON consumable_inbound (consumable_id);
CREATE INDEX idx_consumable_inbound_laboratory_id ON consumable_inbound (laboratory_id);
CREATE INDEX idx_consumable_inbound_operator_user_id ON consumable_inbound (operator_user_id);
CREATE INDEX idx_consumable_inbound_date ON consumable_inbound (inbound_date);
CREATE INDEX idx_consumable_inbound_lab_date ON consumable_inbound (laboratory_id, inbound_date);

CREATE INDEX idx_consumable_outbound_consumable_id ON consumable_outbound (consumable_id);
CREATE INDEX idx_consumable_outbound_laboratory_id ON consumable_outbound (laboratory_id);
CREATE INDEX idx_consumable_outbound_applicant_user_id ON consumable_outbound (applicant_user_id);
CREATE INDEX idx_consumable_outbound_approver_user_id ON consumable_outbound (approver_user_id);
CREATE INDEX idx_consumable_outbound_operator_user_id ON consumable_outbound (operator_user_id);
CREATE INDEX idx_consumable_outbound_date ON consumable_outbound (outbound_date);
CREATE INDEX idx_consumable_outbound_status ON consumable_outbound (outbound_status);
CREATE INDEX idx_consumable_outbound_item_date ON consumable_outbound (consumable_id, outbound_date);

CREATE INDEX idx_hazardous_usage_material_id ON hazardous_material_usage (hazardous_material_id);
CREATE INDEX idx_hazardous_usage_laboratory_id ON hazardous_material_usage (laboratory_id);
CREATE INDEX idx_hazardous_usage_applicant_user_id ON hazardous_material_usage (applicant_user_id);
CREATE INDEX idx_hazardous_usage_approver_user_id ON hazardous_material_usage (approver_user_id);
CREATE INDEX idx_hazardous_usage_operator_user_id ON hazardous_material_usage (operator_user_id);
CREATE INDEX idx_hazardous_usage_date ON hazardous_material_usage (usage_date);
CREATE INDEX idx_hazardous_usage_action_type ON hazardous_material_usage (action_type);
CREATE INDEX idx_hazardous_usage_item_action_date ON hazardous_material_usage (hazardous_material_id, action_type, usage_date);

CREATE INDEX idx_inventory_item_type_item_id_batch ON inventory (item_type, item_id, batch_no);
CREATE INDEX idx_inventory_laboratory_id ON inventory (laboratory_id);
CREATE INDEX idx_inventory_warning_status ON inventory (warning_status);
CREATE INDEX idx_inventory_expiry_date ON inventory (expiry_date);
CREATE INDEX idx_inventory_last_stock_out_time ON inventory (last_stock_out_time);

CREATE INDEX idx_system_log_user_id ON system_log (user_id);
CREATE INDEX idx_system_log_laboratory_id ON system_log (laboratory_id);
CREATE INDEX idx_system_log_operation_time ON system_log (operation_time);
CREATE INDEX idx_system_log_module_name ON system_log (module_name);

-- 视图：显示当前所有借出未归还设备
CREATE OR REPLACE VIEW vw_current_borrowed_equipment AS
SELECT
    eb.id AS borrow_id,
    e.equipment_code,
    e.equipment_name,
    e.model,
    l.laboratory_name,
    u.real_name AS borrower_name,
    eb.borrow_date,
    eb.due_date,
    eb.purpose,
    eb.borrow_status
FROM equipment_borrow eb
INNER JOIN equipment e ON eb.equipment_id = e.id AND e.is_deleted = 0
INNER JOIN laboratory l ON eb.laboratory_id = l.id AND l.is_deleted = 0
INNER JOIN user u ON eb.borrower_user_id = u.id AND u.is_deleted = 0
WHERE eb.is_deleted = 0
  AND eb.borrow_status IN (2, 4)
  AND eb.actual_return_date IS NULL;

-- 视图：显示耗材当前库存与预警状态
CREATE OR REPLACE VIEW vw_consumable_inventory_status AS
SELECT
    c.id AS consumable_id,
    c.consumable_code,
    c.consumable_name,
    c.specification,
    c.unit,
    l.laboratory_name,
    COALESCE(SUM(i.quantity), 0) AS current_quantity,
    c.safety_stock,
    CASE
        WHEN COALESCE(SUM(i.quantity), 0) <= c.safety_stock THEN 'LOW_STOCK'
        ELSE 'NORMAL'
    END AS warning_level
FROM consumable c
INNER JOIN laboratory l ON c.laboratory_id = l.id AND l.is_deleted = 0
LEFT JOIN inventory i
    ON i.item_type = 2
   AND i.item_id = c.id
   AND i.is_deleted = 0
WHERE c.is_deleted = 0
GROUP BY
    c.id,
    c.consumable_code,
    c.consumable_name,
    c.specification,
    c.unit,
    l.laboratory_name,
    c.safety_stock;

-- 视图：显示危化品领用汇总
CREATE OR REPLACE VIEW vw_hazardous_material_usage_summary AS
SELECT
    hm.id AS hazardous_material_id,
    hm.hazardous_code,
    hm.material_name,
    hm.specification,
    hm.unit,
    l.laboratory_name,
    SUM(CASE WHEN hmu.action_type = 2 AND hmu.usage_status = 2 THEN hmu.quantity ELSE 0 END) AS total_used_quantity,
    SUM(CASE WHEN hmu.action_type = 3 AND hmu.usage_status = 2 THEN hmu.quantity ELSE 0 END) AS total_returned_quantity,
    SUM(CASE WHEN hmu.action_type = 4 AND hmu.usage_status = 2 THEN hmu.quantity ELSE 0 END) AS total_waste_quantity
FROM hazardous_material hm
INNER JOIN laboratory l ON hm.laboratory_id = l.id AND l.is_deleted = 0
LEFT JOIN hazardous_material_usage hmu
    ON hmu.hazardous_material_id = hm.id
   AND hmu.is_deleted = 0
WHERE hm.is_deleted = 0
GROUP BY
    hm.id,
    hm.hazardous_code,
    hm.material_name,
    hm.specification,
    hm.unit,
    l.laboratory_name;

DELIMITER $$

-- 存储过程：处理设备借用业务
CREATE PROCEDURE sp_borrow_equipment (
    IN p_equipment_id INT,
    IN p_laboratory_id INT,
    IN p_borrower_user_id INT,
    IN p_approver_user_id INT,
    IN p_purpose VARCHAR(255),
    IN p_borrow_date DATETIME,
    IN p_due_date DATETIME
)
BEGIN
    DECLARE v_status TINYINT;

    SELECT status
    INTO v_status
    FROM equipment
    WHERE id = p_equipment_id
      AND laboratory_id = p_laboratory_id
      AND is_deleted = 0
    FOR UPDATE;

    IF v_status IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Equipment does not exist';
    END IF;

    IF v_status <> 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Equipment is not available';
    END IF;

    INSERT INTO equipment_borrow (
        equipment_id,
        laboratory_id,
        borrower_user_id,
        approver_user_id,
        purpose,
        borrow_date,
        due_date,
        borrow_status
    ) VALUES (
        p_equipment_id,
        p_laboratory_id,
        p_borrower_user_id,
        p_approver_user_id,
        p_purpose,
        p_borrow_date,
        p_due_date,
        2
    );
END$$

-- 存储过程：处理耗材出库业务
CREATE PROCEDURE sp_outbound_consumable (
    IN p_consumable_id INT,
    IN p_laboratory_id INT,
    IN p_applicant_user_id INT,
    IN p_approver_user_id INT,
    IN p_operator_user_id INT,
    IN p_batch_no VARCHAR(100),
    IN p_outbound_type TINYINT,
    IN p_quantity DECIMAL(10,2),
    IN p_outbound_date DATETIME,
    IN p_purpose VARCHAR(255)
)
BEGIN
    DECLARE v_inventory_id INT;
    DECLARE v_current_quantity DECIMAL(10,2);
    DECLARE v_unit_price DECIMAL(10,2);

    SELECT id, quantity, unit_price
    INTO v_inventory_id, v_current_quantity, v_unit_price
    FROM inventory
    WHERE laboratory_id = p_laboratory_id
      AND item_type = 2
      AND item_id = p_consumable_id
      AND batch_no = p_batch_no
      AND is_deleted = 0
    FOR UPDATE;

    IF v_inventory_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Consumable inventory batch does not exist';
    END IF;

    IF v_current_quantity < p_quantity THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Insufficient consumable inventory';
    END IF;

    INSERT INTO consumable_outbound (
        consumable_id,
        laboratory_id,
        applicant_user_id,
        approver_user_id,
        operator_user_id,
        batch_no,
        outbound_type,
        quantity,
        unit_price,
        total_amount,
        outbound_date,
        purpose,
        outbound_status
    ) VALUES (
        p_consumable_id,
        p_laboratory_id,
        p_applicant_user_id,
        p_approver_user_id,
        p_operator_user_id,
        p_batch_no,
        p_outbound_type,
        p_quantity,
        v_unit_price,
        ROUND(v_unit_price * p_quantity, 2),
        p_outbound_date,
        p_purpose,
        2
    );
END$$

-- 存储过程：生成月度耗材消耗报表
CREATE PROCEDURE sp_generate_monthly_consumable_report (
    IN p_year INT,
    IN p_month INT
)
BEGIN
    SELECT
        cc.category_name,
        c.consumable_name,
        c.specification,
        c.unit,
        SUM(co.quantity) AS total_quantity,
        SUM(co.total_amount) AS total_amount
    FROM consumable_outbound co
    INNER JOIN consumable c ON co.consumable_id = c.id AND c.is_deleted = 0
    INNER JOIN consumable_category cc ON c.category_id = cc.id AND cc.is_deleted = 0
    WHERE co.is_deleted = 0
      AND co.outbound_status = 2
      AND YEAR(co.outbound_date) = p_year
      AND MONTH(co.outbound_date) = p_month
    GROUP BY
        cc.category_name,
        c.consumable_name,
        c.specification,
        c.unit
    ORDER BY total_quantity DESC, total_amount DESC;
END$$

-- 触发器：设备借用后自动更新设备状态
CREATE TRIGGER trg_update_equipment_status_on_borrow
AFTER INSERT ON equipment_borrow
FOR EACH ROW
BEGIN
    IF NEW.borrow_status = 2 THEN
        UPDATE equipment
        SET status = 2
        WHERE id = NEW.equipment_id;
    END IF;
END$$

-- 触发器：设备归还后自动恢复设备状态
CREATE TRIGGER trg_restore_equipment_status_on_return
AFTER UPDATE ON equipment_borrow
FOR EACH ROW
BEGIN
    IF NEW.borrow_status = 3 AND NEW.actual_return_date IS NOT NULL THEN
        UPDATE equipment
        SET status = 1
        WHERE id = NEW.equipment_id
          AND status = 2;
    END IF;
END$$

-- 触发器：耗材入库时自动更新库存
CREATE TRIGGER trg_update_inventory_on_consumable_inbound
AFTER INSERT ON consumable_inbound
FOR EACH ROW
BEGIN
    INSERT INTO inventory (
        laboratory_id,
        item_type,
        item_id,
        batch_no,
        quantity,
        locked_quantity,
        unit_price,
        production_date,
        expiry_date,
        last_stock_in_time,
        warning_status,
        remarks
    ) VALUES (
        NEW.laboratory_id,
        2,
        NEW.consumable_id,
        NEW.batch_no,
        NEW.quantity,
        0,
        NEW.unit_price,
        NEW.production_date,
        NEW.expiry_date,
        NEW.inbound_date,
        0,
        'Created by consumable inbound trigger'
    )
    ON DUPLICATE KEY UPDATE
        quantity = quantity + VALUES(quantity),
        unit_price = VALUES(unit_price),
        production_date = COALESCE(VALUES(production_date), production_date),
        expiry_date = COALESCE(VALUES(expiry_date), expiry_date),
        last_stock_in_time = VALUES(last_stock_in_time),
        warning_status = CASE
            WHEN VALUES(expiry_date) IS NOT NULL AND VALUES(expiry_date) < NOW() THEN 3
            WHEN VALUES(expiry_date) IS NOT NULL AND DATEDIFF(VALUES(expiry_date), NOW()) <= 30 THEN 2
            ELSE warning_status
        END,
        update_time = CURRENT_TIMESTAMP;
END$$

-- 触发器：耗材出库时自动更新库存
CREATE TRIGGER trg_update_inventory_on_consumable_outbound
AFTER INSERT ON consumable_outbound
FOR EACH ROW
BEGIN
    IF NEW.outbound_status = 2 THEN
        UPDATE inventory
        SET quantity = quantity - NEW.quantity,
            last_stock_out_time = NEW.outbound_date,
            warning_status = CASE
                WHEN quantity - NEW.quantity <= 0 THEN 1
                ELSE warning_status
            END
        WHERE laboratory_id = NEW.laboratory_id
          AND item_type = 2
          AND item_id = NEW.consumable_id
          AND batch_no = NEW.batch_no
          AND is_deleted = 0;
    END IF;
END$$

-- 触发器：危化品业务时自动更新库存
CREATE TRIGGER trg_update_inventory_on_hazardous_usage
AFTER INSERT ON hazardous_material_usage
FOR EACH ROW
BEGIN
    IF NEW.usage_status = 2 THEN
        IF NEW.action_type = 1 THEN
            INSERT INTO inventory (
                laboratory_id,
                item_type,
                item_id,
                batch_no,
                quantity,
                locked_quantity,
                unit_price,
                last_stock_in_time,
                warning_status,
                remarks
            ) VALUES (
                NEW.laboratory_id,
                3,
                NEW.hazardous_material_id,
                NEW.batch_no,
                NEW.quantity,
                0,
                0,
                NEW.usage_date,
                0,
                'Created by hazardous inbound trigger'
            )
            ON DUPLICATE KEY UPDATE
                quantity = quantity + VALUES(quantity),
                last_stock_in_time = VALUES(last_stock_in_time),
                update_time = CURRENT_TIMESTAMP;
        ELSEIF NEW.action_type IN (2, 4) THEN
            UPDATE inventory
            SET quantity = quantity - NEW.quantity,
                last_stock_out_time = NEW.usage_date
            WHERE laboratory_id = NEW.laboratory_id
              AND item_type = 3
              AND item_id = NEW.hazardous_material_id
              AND batch_no = NEW.batch_no
              AND is_deleted = 0;
        ELSEIF NEW.action_type = 3 THEN
            UPDATE inventory
            SET quantity = quantity + NEW.quantity,
                last_stock_in_time = NEW.usage_date
            WHERE laboratory_id = NEW.laboratory_id
              AND item_type = 3
              AND item_id = NEW.hazardous_material_id
              AND batch_no = NEW.batch_no
              AND is_deleted = 0;
        END IF;
    END IF;
END$$

-- 触发器：记录设备状态变更日志
CREATE TRIGGER trg_log_equipment_status_change
AFTER UPDATE ON equipment
FOR EACH ROW
BEGIN
    IF OLD.status <> NEW.status THEN
        INSERT INTO system_log (
            user_id,
            laboratory_id,
            module_name,
            operation_type,
            business_key,
            operation_desc,
            request_ip,
            operation_status,
            operation_time
        ) VALUES (
            NEW.manager_user_id,
            NEW.laboratory_id,
            'equipment',
            'STATUS_CHANGE',
            NEW.equipment_code,
            CONCAT('设备状态由 ', OLD.status, ' 变更为 ', NEW.status),
            '127.0.0.1',
            1,
            NOW()
        );
    END IF;
END$$

DELIMITER ;

-- 插入角色测试数据
INSERT INTO role (role_code, role_name, description, status) VALUES
('sys_admin', 'System Administrator', '系统管理员', 1),
('lab_director', 'Laboratory Director', '实验室主任', 1),
('equipment_admin', 'Equipment Administrator', '设备管理员', 1),
('consumable_admin', 'Consumable Administrator', '耗材管理员', 1),
('hazardous_admin', 'Hazardous Material Administrator', '危化品管理员', 1),
('teacher', 'Teacher', '普通教师', 1),
('student', 'Student', '学生', 1),
('repair_staff', 'Repair Staff', '维修人员', 1),
('calibration_staff', 'Calibration Staff', '校准人员', 1);

-- 插入实验室测试数据
INSERT INTO laboratory (laboratory_code, laboratory_name, location, contact_phone, safety_level, status) VALUES
('LAB-CHEM-01', 'Analytical Chemistry Lab', 'Science Building A-301', '021-55580001', 3, 1),
('LAB-BIO-01', 'Molecular Biology Lab', 'Science Building B-402', '021-55580002', 2, 1),
('LAB-MAT-01', 'Materials Testing Lab', 'Engineering Building C-208', '021-55580003', 2, 1),
('LAB-PHY-01', 'Physics Instrument Lab', 'Science Building D-115', '021-55580004', 1, 1),
('LAB-ENV-01', 'Environmental Analysis Lab', 'Science Building E-506', '021-55580005', 3, 1);

-- 插入用户测试数据
INSERT INTO user (
    laboratory_id,
    username,
    password_hash,
    real_name,
    user_no,
    gender,
    phone,
    email,
    user_type,
    status,
    last_login_time
) VALUES
(1, 'admin', '$2b$12$3XU1m.DfHWTk7RNgI7/BpO3F6jys/NefkBK3Jl04/ICTVj/OwQTZC', 'Zhang Wei', 'A0001', 1, '13800000001', 'admin@univ.edu.cn', 1, 1, '2026-05-26 08:30:00'),
(1, 'chem_director', '$2a$10$directorhash1', 'Li Ming', 'T1001', 1, '13800000002', 'liming@univ.edu.cn', 2, 1, '2026-05-26 09:00:00'),
(1, 'equip_admin_chem', '$2a$10$eadminhash1', 'Wang Lei', 'T1002', 1, '13800000003', 'wanglei@univ.edu.cn', 3, 1, '2026-05-26 09:30:00'),
(1, 'cons_admin_chem', '$2a$10$cadminhash1', 'Chen Yu', 'T1003', 2, '13800000004', 'chenyu@univ.edu.cn', 4, 1, '2026-05-26 10:00:00'),
(1, 'haz_admin_chem', '$2a$10$hadminhash1', 'Zhao Qian', 'T1004', 2, '13800000005', 'zhaoqian@univ.edu.cn', 5, 1, '2026-05-26 10:30:00'),
(1, 'teacher_liu', '$2a$10$teacherhash1', 'Liu Fang', 'T1005', 2, '13800000006', 'liufang@univ.edu.cn', 6, 1, '2026-05-26 11:00:00'),
(1, 'student_sun', '$2a$10$studenthash1', 'Sun Hao', 'S2023001', 1, '13800000007', 'sunhao@univ.edu.cn', 7, 1, '2026-05-26 11:10:00'),
(1, 'repair_gao', '$2a$10$repairhash1', 'Gao Peng', 'SVC001', 1, '13800000008', 'gaopeng@univ.edu.cn', 8, 1, '2026-05-26 11:20:00'),
(1, 'calib_xu', '$2a$10$calibhash1', 'Xu Ning', 'CAL001', 2, '13800000009', 'xuning@univ.edu.cn', 9, 1, '2026-05-26 11:30:00'),
(2, 'bio_director', '$2a$10$directorhash2', 'He Jia', 'T2001', 2, '13800000010', 'hejia@univ.edu.cn', 2, 1, '2026-05-26 12:00:00'),
(2, 'teacher_qi', '$2a$10$teacherhash2', 'Qi Ran', 'T2002', 1, '13800000011', 'qiran@univ.edu.cn', 6, 1, '2026-05-26 12:10:00'),
(3, 'teacher_ma', '$2a$10$teacherhash3', 'Ma Chen', 'T3001', 1, '13800000012', 'machen@univ.edu.cn', 6, 1, '2026-05-26 12:20:00');

-- 更新实验室主任
UPDATE laboratory SET director_user_id = 2 WHERE id = 1;
UPDATE laboratory SET director_user_id = 10 WHERE id = 2;

-- 插入用户角色测试数据
INSERT INTO user_role (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 5),
(6, 6),
(7, 7),
(8, 8),
(9, 9),
(10, 2),
  (11, 6),
  (12, 6);

INSERT INTO menu (
    id,
    parent_id,
    menu_code,
    menu_name,
    route_path,
    component_name,
    permission_code,
    menu_type,
    sort_order,
    visible,
    status,
    icon,
    remarks
) VALUES
  (1, NULL, 'dashboard', '仪表盘', '/dashboard', 'DashboardView', 'dashboard:view', 1, 10, 1, 1, 'DataBoard', 'dashboard'),
  (2, NULL, 'system_group', '系统管理', '/system', NULL, 'system:view', 1, 20, 1, 1, 'Setting', 'system group'),
  (3, 2, 'laboratory', '实验室管理', '/laboratories', 'LaboratoryView', 'laboratory:view', 1, 21, 1, 1, 'OfficeBuilding', 'laboratory management'),
  (4, 2, 'user', '用户管理', '/users', 'UserView', 'user:view', 1, 22, 1, 1, 'UserFilled', 'user management'),
  (5, 2, 'role_management', '角色与菜单', '/roles', 'RoleManagementView', 'role:view', 1, 23, 1, 1, 'Lock', 'role and menu assignment'),
  (6, NULL, 'asset_group', '资产档案', '/assets', NULL, 'asset:view', 1, 30, 1, 1, 'Management', 'asset group'),
  (7, 6, 'equipment_category', '设备分类', '/equipment-categories', 'EquipmentCategoryView', 'equipment_category:view', 1, 31, 1, 1, 'Grid', 'equipment category'),
  (8, 6, 'equipment', '设备台账', '/equipment', 'EquipmentView', 'equipment:view', 1, 32, 1, 1, 'Cpu', 'equipment ledger'),
  (9, 6, 'consumable_category', '耗材分类', '/consumable-categories', 'ConsumableCategoryView', 'consumable_category:view', 1, 33, 1, 1, 'CollectionTag', 'consumable category'),
  (10, 6, 'consumable', '耗材台账', '/consumables', 'ConsumableView', 'consumable:view', 1, 34, 1, 1, 'Box', 'consumable ledger'),
  (11, 6, 'hazardous_material', '危化品台账', '/hazardous-materials', 'HazardousMaterialView', 'hazardous_material:view', 1, 35, 1, 1, 'WarningFilled', 'hazardous ledger'),
  (12, NULL, 'business_group', '业务流程', '/business', NULL, 'business:view', 1, 40, 1, 1, 'Memo', 'business group'),
  (13, 12, 'equipment_borrow', '设备借用', '/equipment-borrows', 'EquipmentBorrowBusinessView', 'equipment_borrow:view', 1, 41, 1, 1, 'Tickets', 'equipment borrow'),
  (14, 12, 'equipment_repair', '设备维修', '/equipment-repairs', 'EquipmentRepairView', 'equipment_repair:view', 1, 42, 1, 1, 'Tools', 'equipment repair'),
  (15, 12, 'equipment_calibration', '设备校准', '/equipment-calibrations', 'EquipmentCalibrationView', 'equipment_calibration:view', 1, 43, 1, 1, 'SetUp', 'equipment calibration'),
  (16, 12, 'consumable_inbound', '耗材入库', '/consumable-inbounds', 'ConsumableInboundBusinessView', 'consumable_inbound:view', 1, 44, 1, 1, 'Download', 'consumable inbound'),
  (17, 12, 'consumable_outbound', '耗材出库', '/consumable-outbounds', 'ConsumableOutboundBusinessView', 'consumable_outbound:view', 1, 45, 1, 1, 'Upload', 'consumable outbound'),
  (18, 12, 'hazardous_usage', '危化品领用', '/hazardous-usages', 'HazardousUsageBusinessView', 'hazardous_usage:view', 1, 46, 1, 1, 'Operation', 'hazardous usage'),
  (19, NULL, 'report_center', '报表中心', '/reports', 'ReportCenterView', 'report:view', 1, 50, 1, 1, 'Histogram', 'report center'),
  (20, NULL, 'inventory_option', '/inventory/options', '/inventory/options', NULL, 'inventory:view', 2, 1000, 0, 1, NULL, 'inventory permission'),
  (21, NULL, 'laboratory_edit', '/laboratories edit', '/laboratories', NULL, 'laboratory:edit', 2, 1010, 0, 1, NULL, 'laboratory write'),
  (22, NULL, 'user_edit', '/users edit', '/users', NULL, 'user:edit', 2, 1020, 0, 1, NULL, 'user write'),
  (23, NULL, 'role_edit', '/roles edit', '/roles', NULL, 'role:edit', 2, 1025, 0, 1, NULL, 'role write'),
  (24, NULL, 'menu_view', '/menus tree', '/menus/tree', NULL, 'menu:view', 2, 1026, 0, 1, NULL, 'menu tree access'),
  (25, NULL, 'equipment_category_edit', '/equipment-categories edit', '/equipment-categories', NULL, 'equipment_category:edit', 2, 1030, 0, 1, NULL, 'equipment category write'),
  (26, NULL, 'equipment_edit', '/equipment edit', '/equipment', NULL, 'equipment:edit', 2, 1040, 0, 1, NULL, 'equipment write'),
  (27, NULL, 'equipment_borrow_edit', '/equipment-borrows edit', '/equipment-borrows', NULL, 'equipment_borrow:edit', 2, 1050, 0, 1, NULL, 'equipment borrow write'),
  (28, NULL, 'equipment_repair_edit', '/equipment-repairs edit', '/equipment-repairs', NULL, 'equipment_repair:edit', 2, 1060, 0, 1, NULL, 'equipment repair write'),
  (29, NULL, 'equipment_calibration_edit', '/equipment-calibrations edit', '/equipment-calibrations', NULL, 'equipment_calibration:edit', 2, 1070, 0, 1, NULL, 'equipment calibration write'),
  (30, NULL, 'consumable_category_edit', '/consumable-categories edit', '/consumable-categories', NULL, 'consumable_category:edit', 2, 1080, 0, 1, NULL, 'consumable category write'),
  (31, NULL, 'consumable_edit', '/consumables edit', '/consumables', NULL, 'consumable:edit', 2, 1090, 0, 1, NULL, 'consumable write'),
  (32, NULL, 'consumable_inbound_edit', '/consumable-inbounds edit', '/consumable-inbounds', NULL, 'consumable_inbound:edit', 2, 1100, 0, 1, NULL, 'consumable inbound write'),
  (33, NULL, 'consumable_outbound_edit', '/consumable-outbounds edit', '/consumable-outbounds', NULL, 'consumable_outbound:edit', 2, 1110, 0, 1, NULL, 'consumable outbound write'),
  (34, NULL, 'hazardous_material_edit', '/hazardous-materials edit', '/hazardous-materials', NULL, 'hazardous_material:edit', 2, 1120, 0, 1, NULL, 'hazardous write'),
  (35, NULL, 'hazardous_usage_edit', '/hazardous-usages edit', '/hazardous-usages', NULL, 'hazardous_usage:edit', 2, 1130, 0, 1, NULL, 'hazardous usage write'),
  (36, NULL, 'report_export', '/reports export', '/reports', NULL, 'report:export', 2, 1140, 0, 1, NULL, 'report export');

INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id FROM role r INNER JOIN menu m WHERE r.role_code = 'sys_admin';

INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id FROM role r INNER JOIN menu m
WHERE r.role_code = 'lab_director'
  AND m.menu_code IN (
      'dashboard', 'system_group', 'laboratory', 'user', 'role_management', 'asset_group',
      'equipment_category', 'equipment', 'consumable_category', 'consumable', 'hazardous_material',
      'business_group', 'equipment_borrow', 'equipment_repair', 'equipment_calibration',
      'consumable_inbound', 'consumable_outbound', 'hazardous_usage', 'report_center',
      'inventory_option', 'laboratory_edit', 'user_edit', 'role_edit', 'menu_view',
      'equipment_category_edit', 'equipment_edit', 'equipment_borrow_edit', 'equipment_repair_edit',
      'equipment_calibration_edit', 'consumable_category_edit', 'consumable_edit',
      'consumable_inbound_edit', 'consumable_outbound_edit', 'hazardous_material_edit',
      'hazardous_usage_edit', 'report_export'
  );

INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id FROM role r INNER JOIN menu m
WHERE r.role_code = 'equipment_admin'
  AND m.menu_code IN (
      'dashboard', 'asset_group', 'equipment_category', 'equipment', 'business_group',
      'equipment_borrow', 'equipment_repair', 'equipment_calibration', 'report_center',
      'equipment_category_edit', 'equipment_edit', 'equipment_borrow_edit',
      'equipment_repair_edit', 'equipment_calibration_edit', 'report_export'
  );

INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id FROM role r INNER JOIN menu m
WHERE r.role_code = 'consumable_admin'
  AND m.menu_code IN (
      'dashboard', 'asset_group', 'consumable_category', 'consumable', 'business_group',
      'consumable_inbound', 'consumable_outbound', 'report_center', 'inventory_option',
      'consumable_category_edit', 'consumable_edit', 'consumable_inbound_edit',
      'consumable_outbound_edit', 'report_export'
  );

INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id FROM role r INNER JOIN menu m
WHERE r.role_code = 'hazardous_admin'
  AND m.menu_code IN (
      'dashboard', 'asset_group', 'hazardous_material', 'business_group', 'hazardous_usage',
      'report_center', 'inventory_option', 'hazardous_material_edit',
      'hazardous_usage_edit', 'report_export'
  );

INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id FROM role r INNER JOIN menu m
WHERE r.role_code = 'teacher'
  AND m.menu_code IN (
      'dashboard', 'asset_group', 'equipment', 'consumable', 'hazardous_material',
      'business_group', 'equipment_borrow', 'equipment_repair', 'consumable_outbound',
      'hazardous_usage', 'report_center', 'inventory_option', 'equipment_borrow_edit',
      'equipment_repair_edit', 'consumable_outbound_edit', 'hazardous_usage_edit',
      'report_export'
  );

INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id FROM role r INNER JOIN menu m
WHERE r.role_code = 'student'
  AND m.menu_code IN (
      'dashboard', 'asset_group', 'equipment', 'consumable', 'business_group',
      'equipment_borrow', 'consumable_outbound', 'equipment_borrow_edit',
      'consumable_outbound_edit', 'inventory_option'
  );

INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id FROM role r INNER JOIN menu m
WHERE r.role_code = 'repair_staff'
  AND m.menu_code IN (
      'dashboard', 'asset_group', 'equipment', 'business_group', 'equipment_repair',
      'report_center', 'equipment_repair_edit', 'report_export'
  );

INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id FROM role r INNER JOIN menu m
WHERE r.role_code = 'calibration_staff'
  AND m.menu_code IN (
      'dashboard', 'asset_group', 'equipment', 'business_group', 'equipment_calibration',
      'report_center', 'equipment_calibration_edit', 'report_export'
  );

-- 插入设备类别测试数据
INSERT INTO equipment_category (category_code, category_name, description, status) VALUES
('EQ-ANALYSIS', 'Analytical Instrument', '分析仪器', 1),
('EQ-GENERAL', 'General Equipment', '通用设备', 1),
('EQ-GLASS', 'Glassware Equipment', '玻璃器皿设备', 1),
('EQ-PRECISION', 'Precision Instrument', '精密仪器', 1),
('EQ-SAFETY', 'Safety Equipment', '安全设备', 1);

-- 插入设备测试数据
INSERT INTO equipment (
    laboratory_id,
    category_id,
    manager_user_id,
    equipment_code,
    equipment_name,
    model,
    brand,
    manufacturer,
    purchase_date,
    purchase_price,
    service_life_years,
    storage_location,
    status,
    calibration_cycle_days,
    last_calibration_date,
    next_calibration_date,
    remarks
) VALUES
(1, 1, 3, 'EQ-CHEM-0001', 'UV-Vis Spectrophotometer', 'UV-2600', 'Shimadzu', 'Shimadzu China', '2022-03-15 00:00:00', 85000.00, 8, 'A301-Instrument Room 1', 1, 365, '2025-12-20 09:00:00', '2026-12-20 09:00:00', '教学与科研共用'),
(1, 4, 3, 'EQ-CHEM-0002', 'Analytical Balance', 'ME204E', 'Mettler Toledo', 'Mettler Toledo', '2021-09-10 00:00:00', 18000.00, 10, 'A301-Preparation Room', 1, 180, '2026-01-10 10:00:00', '2026-07-09 10:00:00', '高精度称量'),
(1, 1, 3, 'EQ-CHEM-0003', 'Gas Chromatograph', 'GC-2014', 'Shimadzu', 'Shimadzu China', '2020-06-18 00:00:00', 126000.00, 10, 'A301-Instrument Room 2', 1, 365, '2025-11-11 09:00:00', '2026-11-11 09:00:00', '已接入氢火焰检测器'),
(2, 4, NULL, 'EQ-BIO-0001', 'PCR Instrument', 'T100', 'Bio-Rad', 'Bio-Rad Labs', '2023-04-01 00:00:00', 42000.00, 8, 'B402-PCR Room', 1, 365, '2026-03-01 09:00:00', '2027-03-01 09:00:00', '分子实验课程使用'),
(3, 2, NULL, 'EQ-MAT-0001', 'Drying Oven', 'DHG-9070A', 'Yiheng', 'Shanghai Yiheng', '2019-10-12 00:00:00', 9800.00, 8, 'C208-Sample Room', 1, 365, '2025-10-01 09:00:00', '2026-10-01 09:00:00', '材料样品前处理'),
(4, 2, NULL, 'EQ-PHY-0001', 'Digital Oscilloscope', 'DS1054Z', 'Rigol', 'Rigol Technologies', '2024-02-20 00:00:00', 5800.00, 6, 'D115-Workbench 3', 1, 365, '2026-02-01 09:00:00', '2027-02-01 09:00:00', '电子实验教学'),
(5, 1, NULL, 'EQ-ENV-0001', 'Ion Chromatograph', 'CIC-D160', 'Qingdao Shenghan', 'Qingdao Shenghan', '2021-07-05 00:00:00', 138000.00, 10, 'E506-Instrument Room', 1, 365, '2025-08-20 09:00:00', '2026-08-20 09:00:00', '环境水样分析');

-- 插入耗材类别测试数据
INSERT INTO consumable_category (category_code, category_name, description, status) VALUES
('CON-REAGENT', 'Chemical Reagent', '化学试剂类', 1),
('CON-FILTER', 'Filter Material', '过滤与分离耗材', 1),
('CON-GLASS', 'Glassware', '玻璃器皿', 1),
('CON-BIO', 'Biology Consumable', '生物实验耗材', 1),
('CON-SAFETY', 'Safety Consumable', '安全防护耗材', 1);

-- 插入耗材测试数据
INSERT INTO consumable (
    laboratory_id,
    category_id,
    manager_user_id,
    consumable_code,
    consumable_name,
    specification,
    unit,
    unit_price,
    safety_stock,
    max_stock,
    storage_location,
    expiry_required,
    status,
    remarks
) VALUES
(1, 1, 4, 'CON-CHEM-0001', 'Sodium Chloride', '500g/AR', 'bottle', 18.50, 10.00, 100.00, 'A301-Reagent Cabinet 1', 1, 1, '常用基础试剂'),
(1, 1, 4, 'CON-CHEM-0002', 'Methanol', '500mL/HPLC', 'bottle', 45.00, 8.00, 60.00, 'A301-Solvent Cabinet', 1, 1, '色谱纯甲醇'),
(1, 2, 4, 'CON-CHEM-0003', 'Syringe Filter', '0.22um/50pcs', 'box', 85.00, 5.00, 40.00, 'A301-Consumable Shelf 2', 1, 1, '样品预处理'),
(1, 3, 4, 'CON-CHEM-0004', 'Volumetric Flask', '100mL', 'piece', 26.00, 20.00, 200.00, 'A301-Glassware Cabinet', 0, 1, '玻璃器皿'),
(2, 4, NULL, 'CON-BIO-0001', 'Pipette Tip', '200uL/1000pcs', 'box', 32.00, 15.00, 120.00, 'B402-Consumable Room', 1, 1, '无菌吸头'),
(3, 5, NULL, 'CON-MAT-0001', 'Nitrile Gloves', 'M/100pcs', 'box', 28.00, 12.00, 100.00, 'C208-Safety Shelf', 1, 1, '通用防护耗材');

-- 插入补充耗材测试数据
INSERT INTO consumable (
    laboratory_id,
    category_id,
    manager_user_id,
    consumable_code,
    consumable_name,
    specification,
    unit,
    unit_price,
    safety_stock,
    max_stock,
    storage_location,
    expiry_required,
    status,
    remarks
) VALUES
(1, 5, 4, 'CON-CHEM-0005', 'Protective Mask', 'KN95/20pcs', 'box', 42.00, 6.00, 80.00, 'A301-Safety Cabinet', 1, 1, '实验室防护口罩'),
(5, 2, NULL, 'CON-ENV-0001', 'Water Quality Test Strip', '50pcs/box', 'box', 58.00, 8.00, 60.00, 'E506-Consumable Cabinet', 1, 1, '环境检测快检耗材');

-- 插入危化品测试数据
INSERT INTO hazardous_material (
    laboratory_id,
    manager_user_id,
    hazardous_code,
    material_name,
    cas_no,
    hazard_category,
    specification,
    unit,
    concentration,
    storage_location,
    safety_stock,
    msds_code,
    status,
    remarks
) VALUES
(1, 5, 'HAZ-CHEM-0001', 'Hydrochloric Acid', '7647-01-0', 'Corrosive', '500mL', 'bottle', '36%-38%', 'A301-Hazard Cabinet 1', 5.00, 'MSDS-HCL-001', 2, '强腐蚀性'),
(1, 5, 'HAZ-CHEM-0002', 'Sulfuric Acid', '7664-93-9', 'Corrosive', '500mL', 'bottle', '95%-98%', 'A301-Hazard Cabinet 1', 4.00, 'MSDS-H2SO4-001', 2, '高危酸类'),
(1, 5, 'HAZ-CHEM-0003', 'Acetone', '67-64-1', 'Flammable Liquid', '500mL', 'bottle', 'AR', 'A301-Flammable Cabinet', 6.00, 'MSDS-ACE-001', 2, '易燃溶剂'),
(5, NULL, 'HAZ-ENV-0001', 'Potassium Dichromate', '7778-50-9', 'Oxidizing Toxic', '100g', 'bottle', 'AR', 'E506-Hazard Cabinet', 3.00, 'MSDS-K2CR2O7-001', 2, '环境分析重铬酸盐'),
(2, NULL, 'HAZ-BIO-0001', 'Ethidium Bromide', '1239-45-8', 'Toxic', '25g', 'bottle', 'Molecular Biology Grade', 'B402-Hazard Shelf', 2.00, 'MSDS-EB-001', 2, '核酸染料');

-- 插入设备借用测试数据
INSERT INTO equipment_borrow (
    equipment_id,
    laboratory_id,
    borrower_user_id,
    approver_user_id,
    purpose,
    borrow_date,
    due_date,
    actual_return_date,
    borrow_status,
    return_condition,
    remarks
) VALUES
(1, 1, 6, 2, '本科分析化学实验课程吸光度测定', '2026-05-10 09:00:00', '2026-05-12 17:00:00', '2026-05-12 16:30:00', 3, '完好归还', '课程教学'),
(2, 1, 7, 3, '毕业论文样品称量', '2026-05-18 13:30:00', '2026-05-20 18:00:00', NULL, 2, NULL, '学生需指导教师授权'),
(3, 1, 6, 2, '科研项目挥发性组分分析', '2026-05-21 10:00:00', '2026-05-26 18:00:00', NULL, 4, NULL, '已逾期未归还'),
(4, 2, 11, 10, 'PCR扩增实验教学', '2026-05-15 09:00:00', '2026-05-16 17:00:00', '2026-05-16 16:00:00', 3, '运行正常', '实验课'),
(6, 4, 7, 2, '物理实验波形采集', '2026-05-22 14:00:00', '2026-05-23 18:00:00', '2026-05-23 17:30:00', 3, '完好', '跨实验室借用'),
(7, 5, 12, 10, '环境样品离子分析', '2026-05-24 08:30:00', '2026-05-30 17:30:00', NULL, 2, NULL, '科研检测');

-- 插入补充设备借用测试数据
INSERT INTO equipment_borrow (
    equipment_id,
    laboratory_id,
    borrower_user_id,
    approver_user_id,
    purpose,
    borrow_date,
    due_date,
    actual_return_date,
    borrow_status,
    return_condition,
    remarks
) VALUES
(1, 1, 7, 3, '样品比色测定复测', '2026-05-25 09:00:00', '2026-05-29 17:00:00', NULL, 2, NULL, '毕业设计加测'),
(4, 2, 11, 10, '扩增条件优化实验', '2026-05-26 08:30:00', '2026-05-31 18:00:00', NULL, 2, NULL, '研究生课程实验'),
(5, 3, 12, 10, '材料样品干燥前处理', '2026-05-19 10:00:00', '2026-05-24 17:30:00', NULL, 4, NULL, '已逾期待归还');

-- 根据借用数据同步设备状态
UPDATE equipment SET status = 2 WHERE id IN (1, 2, 3, 4, 5, 7);

-- 插入设备维修测试数据
INSERT INTO equipment_repair (
    equipment_id,
    laboratory_id,
    reporter_user_id,
    repair_user_id,
    fault_description,
    report_time,
    repair_start_time,
    repair_end_time,
    repair_status,
    repair_cost,
    repair_result,
    remarks
) VALUES
(3, 1, 3, 8, '进样口温度异常，基线波动明显', '2025-11-20 09:20:00', '2025-11-20 14:00:00', '2025-11-21 16:00:00', 3, 1800.00, '更换温控模块后恢复正常', '已完成'),
(3, 1, 6, 8, '检测器信号不稳定', '2026-03-12 10:00:00', '2026-03-12 15:30:00', '2026-03-13 17:00:00', 3, 2200.00, '清洁检测器并更换密封圈', '已完成'),
(2, 1, 7, 8, '称量显示漂移', '2026-01-05 11:00:00', '2026-01-05 15:00:00', '2026-01-06 09:30:00', 3, 350.00, '重新校准后恢复', '轻微故障'),
(1, 1, 3, 8, '光源老化导致吸光度不稳定', '2026-02-18 08:40:00', '2026-02-18 13:00:00', '2026-02-19 10:00:00', 3, 2600.00, '更换氘灯', '耗材更换'),
(5, 3, 12, 8, '加热温度偏差过大', '2026-04-03 09:10:00', '2026-04-03 13:30:00', '2026-04-04 11:00:00', 3, 900.00, '更换温控探头', '已恢复'),
(3, 1, 3, 8, '点火失败', '2026-05-01 10:30:00', '2026-05-01 14:00:00', '2026-05-02 15:30:00', 3, 1200.00, '更换点火组件', '近期第三次维修');

-- 插入补充设备维修测试数据
INSERT INTO equipment_repair (
    equipment_id,
    laboratory_id,
    reporter_user_id,
    repair_user_id,
    fault_description,
    report_time,
    repair_start_time,
    repair_end_time,
    repair_status,
    repair_cost,
    repair_result,
    remarks
) VALUES
(7, 5, 12, 8, '淋洗液压力异常', '2026-02-14 09:00:00', '2026-02-14 13:00:00', '2026-02-15 11:30:00', 3, 1600.00, '更换泵密封组件', '环境分析设备维修'),
(4, 2, 11, 8, '模块升降温速度偏慢', '2026-03-28 10:00:00', '2026-03-28 15:00:00', '2026-03-29 10:00:00', 3, 780.00, '散热风扇更换', 'PCR设备维护'),
(1, 1, 6, 8, '波长切换卡顿', '2026-04-25 08:50:00', '2026-04-25 14:00:00', '2026-04-26 10:20:00', 3, 980.00, '更换步进电机', '仪器恢复正常'),
(6, 4, 7, 8, '探头接触不良', '2026-05-05 14:30:00', '2026-05-05 16:00:00', '2026-05-06 09:30:00', 3, 260.00, '焊点修复', '示波器维修');

-- 插入设备校准测试数据
INSERT INTO equipment_calibration (
    equipment_id,
    laboratory_id,
    calibration_user_id,
    certificate_no,
    calibration_date,
    valid_until,
    calibration_result,
    calibration_status,
    remarks
) VALUES
(1, 1, 9, 'CAL-2025-UV2600-001', '2025-12-20 09:00:00', '2026-12-20 09:00:00', 1, 2, '校准合格'),
(2, 1, 9, 'CAL-2026-ME204E-001', '2026-01-10 10:00:00', '2026-07-09 10:00:00', 1, 2, '半年校准'),
(3, 1, 9, 'CAL-2025-GC2014-001', '2025-11-11 09:00:00', '2026-11-11 09:00:00', 1, 2, '校准合格'),
(4, 2, 9, 'CAL-2026-T100-001', '2026-03-01 09:00:00', '2027-03-01 09:00:00', 1, 2, '温控精度合格'),
(5, 3, 9, 'CAL-2025-DHG9070A-001', '2025-10-01 09:00:00', '2026-10-01 09:00:00', 1, 2, '箱温均匀性合格'),
(7, 5, 9, 'CAL-2025-CICD160-001', '2025-08-20 09:00:00', '2026-08-20 09:00:00', 1, 2, '离子分析校准合格');

-- 插入耗材入库测试数据
INSERT INTO consumable_inbound (
    consumable_id,
    laboratory_id,
    operator_user_id,
    batch_no,
    inbound_type,
    quantity,
    unit_price,
    total_amount,
    production_date,
    expiry_date,
    supplier_name,
    inbound_date,
    remarks
) VALUES
(1, 1, 4, 'BATCH-NACL-202601', 1, 40.00, 18.50, 740.00, '2026-01-05 00:00:00', '2028-01-04 00:00:00', 'Shanghai Reagent Co.', '2026-01-12 10:00:00', '春季学期采购'),
(2, 1, 4, 'BATCH-MEOH-202602', 1, 20.00, 45.00, 900.00, '2026-02-01 00:00:00', '2027-08-01 00:00:00', 'Sinopharm Chemical Reagent', '2026-02-10 09:00:00', '色谱分析专用'),
(3, 1, 4, 'BATCH-SF-202603', 1, 15.00, 85.00, 1275.00, '2026-03-01 00:00:00', '2027-03-01 00:00:00', 'Millipore Distributor', '2026-03-05 14:00:00', '科研耗材'),
(4, 1, 4, 'BATCH-VF-202604', 1, 80.00, 26.00, 2080.00, '2026-04-01 00:00:00', NULL, 'Glassware Supplier Co.', '2026-04-08 15:00:00', '玻璃器皿补充'),
(5, 2, 10, 'BATCH-TIP-202601', 1, 60.00, 32.00, 1920.00, '2026-01-20 00:00:00', '2028-01-19 00:00:00', 'BioLab Supplies', '2026-01-25 11:00:00', '无菌吸头'),
(6, 3, 12, 'BATCH-GLOVE-202602', 1, 50.00, 28.00, 1400.00, '2026-02-10 00:00:00', '2027-02-10 00:00:00', 'Safety Protect Co.', '2026-02-18 10:30:00', '劳保用品'),
(1, 1, 4, 'BATCH-NACL-202605', 1, 25.00, 19.00, 475.00, '2026-05-01 00:00:00', '2028-04-30 00:00:00', 'Shanghai Reagent Co.', '2026-05-06 09:30:00', '追加采购'),
(7, 5, 10, 'BATCH-STRIP-202605', 1, 18.00, 58.00, 1044.00, '2026-05-05 00:00:00', '2026-08-15 00:00:00', 'Eco Lab Supply', '2026-05-09 10:10:00', '环境耗材补货'),
(2, 1, 4, 'BATCH-MEOH-202605', 1, 10.00, 46.00, 460.00, '2026-05-12 00:00:00', '2026-07-20 00:00:00', 'Sinopharm Chemical Reagent', '2026-05-14 16:20:00', '短期项目采购'),
(5, 2, 10, 'BATCH-TIP-202605', 1, 25.00, 31.50, 787.50, '2026-05-08 00:00:00', '2026-07-10 00:00:00', 'BioLab Supplies', '2026-05-15 09:40:00', '期末教学补货'),
(6, 3, 12, 'BATCH-GLOVE-202605', 1, 20.00, 29.00, 580.00, '2026-05-10 00:00:00', '2026-06-25 00:00:00', 'Safety Protect Co.', '2026-05-18 13:20:00', '短期防护补充');

-- 插入耗材出库测试数据
INSERT INTO consumable_outbound (
    consumable_id,
    laboratory_id,
    applicant_user_id,
    approver_user_id,
    operator_user_id,
    batch_no,
    outbound_type,
    quantity,
    unit_price,
    total_amount,
    outbound_date,
    purpose,
    outbound_status,
    remarks
) VALUES
(1, 1, 6, 4, 4, 'BATCH-NACL-202601', 1, 8.00, 18.50, 148.00, '2026-03-15 09:00:00', '分析化学实验课程配置标准溶液', 2, '教学领用'),
(2, 1, 6, 4, 4, 'BATCH-MEOH-202602', 2, 5.00, 45.00, 225.00, '2026-03-20 14:00:00', '色谱流动相配置', 2, '科研领用'),
(3, 1, 7, 4, 4, 'BATCH-SF-202603', 2, 3.00, 85.00, 255.00, '2026-04-12 10:30:00', '样品过滤处理', 2, '毕业论文使用'),
(4, 1, 6, 4, 4, 'BATCH-VF-202604', 1, 12.00, 26.00, 312.00, '2026-04-20 15:00:00', '容量分析实验器皿使用', 2, '教学使用'),
(1, 1, 7, 4, 4, 'BATCH-NACL-202605', 2, 4.00, 19.00, 76.00, '2026-05-10 09:20:00', '毕业设计样品配制', 2, '学生科研'),
(5, 2, 11, 10, 10, 'BATCH-TIP-202601', 1, 10.00, 32.00, 320.00, '2026-05-12 11:00:00', 'PCR教学实验', 2, '实验课'),
(6, 3, 12, 10, 10, 'BATCH-GLOVE-202602', 3, 6.00, 28.00, 168.00, '2026-05-18 08:40:00', '材料测试防护使用', 2, '劳保发放'),
(2, 1, 6, 4, 4, 'BATCH-MEOH-202605', 2, 2.00, 46.00, 92.00, '2026-05-16 14:10:00', '样品萃取试验', 2, '短期项目使用'),
(5, 2, 11, 10, 10, 'BATCH-TIP-202605', 1, 6.00, 31.50, 189.00, '2026-05-19 10:15:00', '分子实验课程补充', 2, '教学使用'),
(6, 3, 12, 10, 10, 'BATCH-GLOVE-202605', 3, 4.00, 29.00, 116.00, '2026-05-20 09:05:00', '访客测试防护', 2, '防护用品发放'),
(7, 5, 12, 10, 10, 'BATCH-STRIP-202605', 2, 3.00, 58.00, 174.00, '2026-05-22 11:20:00', '地表水快速检测', 2, '环境监测');

-- 插入危化品使用测试数据
INSERT INTO hazardous_material_usage (
    hazardous_material_id,
    laboratory_id,
    applicant_user_id,
    approver_user_id,
    operator_user_id,
    action_type,
    batch_no,
    quantity,
    remaining_quantity,
    usage_date,
    purpose,
    project_name,
    witness_name,
    usage_status,
    remarks
) VALUES
(1, 1, 5, 2, 5, 1, 'HZ-HCL-202601', 12.00, 12.00, '2026-01-18 09:00:00', '采购入库', '基础化学平台', 'Zhao Qian', 2, '盐酸批次入库'),
(2, 1, 5, 2, 5, 1, 'HZ-H2SO4-202602', 10.00, 10.00, '2026-02-08 09:00:00', '采购入库', '基础化学平台', 'Zhao Qian', 2, '硫酸批次入库'),
(3, 1, 5, 2, 5, 1, 'HZ-ACE-202603', 15.00, 15.00, '2026-03-02 09:00:00', '采购入库', '有机分析平台', 'Zhao Qian', 2, '丙酮批次入库'),
(1, 1, 6, 2, 5, 2, 'HZ-HCL-202601', 2.00, 0.50, '2026-04-06 10:00:00', '酸碱滴定实验', '化学实验教学A', 'Li Ming', 2, '教师领用'),
(1, 1, 6, 2, 5, 3, 'HZ-HCL-202601', 0.50, 0.50, '2026-04-06 17:30:00', '余量归还', '化学实验教学A', 'Li Ming', 2, '归还未用完余量'),
(2, 1, 6, 2, 5, 2, 'HZ-H2SO4-202602', 1.50, 0.20, '2026-04-20 09:40:00', '样品消解实验', '科研项目X1', 'Li Ming', 2, '硫酸领用'),
(2, 1, 6, 2, 5, 4, 'HZ-H2SO4-202602', 0.20, 0.00, '2026-04-20 18:00:00', '废液处理', '科研项目X1', 'Li Ming', 2, '废液交接'),
(3, 1, 7, 2, 5, 2, 'HZ-ACE-202603', 3.00, 0.30, '2026-05-08 13:30:00', '有机样品清洗', '本科毕设B3', 'Li Ming', 2, '学生在教师授权下领用'),
(3, 1, 7, 2, 5, 3, 'HZ-ACE-202603', 0.30, 0.30, '2026-05-08 17:50:00', '余量归还', '本科毕设B3', 'Li Ming', 2, '余量回收'),
(4, 5, 12, 10, 10, 1, 'HZ-K2CR2O7-202604', 5.00, 5.00, '2026-04-15 10:00:00', '采购入库', '环境分析平台', 'He Jia', 2, '重铬酸钾入库'),
(4, 5, 12, 10, 10, 2, 'HZ-K2CR2O7-202604', 1.00, 0.10, '2026-05-16 09:00:00', 'COD测定实验', '水质分析项目', 'He Jia', 2, '领用记录'),
(5, 2, 11, 10, 10, 1, 'HZ-EB-202602', 3.00, 3.00, '2026-02-12 09:30:00', '采购入库', '分子平台', 'He Jia', 2, 'EB入库'),
(1, 1, 5, 2, 5, 1, 'HZ-HCL-202605', 6.00, 6.00, '2026-05-11 09:00:00', '采购入库', '化学课程补货', 'Zhao Qian', 2, '盐酸追加入库'),
(1, 1, 6, 2, 5, 2, 'HZ-HCL-202605', 1.20, 0.10, '2026-05-18 10:00:00', '无机实验用酸', '化学实验教学B', 'Li Ming', 2, '新增领用'),
(5, 2, 11, 10, 10, 2, 'HZ-EB-202602', 0.60, 0.05, '2026-05-20 14:00:00', '凝胶电泳染色', '分子实验课程', 'He Jia', 2, '教学领用');

-- 更新危化品库存扩展字段
UPDATE inventory
SET production_date = '2026-01-05 00:00:00',
    expiry_date = '2027-01-04 00:00:00',
    unit_price = 120.00
WHERE item_type = 3 AND item_id = 1 AND batch_no = 'HZ-HCL-202601';

UPDATE inventory
SET production_date = '2026-02-01 00:00:00',
    expiry_date = '2027-02-01 00:00:00',
    unit_price = 135.00
WHERE item_type = 3 AND item_id = 2 AND batch_no = 'HZ-H2SO4-202602';

UPDATE inventory
SET production_date = '2026-03-01 00:00:00',
    expiry_date = '2026-06-15 00:00:00',
    unit_price = 55.00
WHERE item_type = 3 AND item_id = 3 AND batch_no = 'HZ-ACE-202603';

UPDATE inventory
SET production_date = '2026-04-01 00:00:00',
    expiry_date = '2026-06-10 00:00:00',
    unit_price = 88.00
WHERE item_type = 3 AND item_id = 4 AND batch_no = 'HZ-K2CR2O7-202604';

UPDATE inventory
SET production_date = '2026-02-10 00:00:00',
    expiry_date = '2026-07-01 00:00:00',
    unit_price = 260.00
WHERE item_type = 3 AND item_id = 5 AND batch_no = 'HZ-EB-202602';

UPDATE inventory
SET production_date = '2026-05-10 00:00:00',
    expiry_date = '2026-06-30 00:00:00',
    unit_price = 118.00
WHERE item_type = 3 AND item_id = 1 AND batch_no = 'HZ-HCL-202605';

-- 插入系统日志测试数据
INSERT INTO system_log (
    user_id,
    laboratory_id,
    module_name,
    operation_type,
    business_key,
    operation_desc,
    request_ip,
    operation_status,
    operation_time
) VALUES
(1, 1, 'user', 'CREATE', 'user:teacher_liu', '创建教师账号 teacher_liu', '127.0.0.1', 1, '2026-01-10 09:00:00'),
(3, 1, 'equipment', 'UPDATE', 'EQ-CHEM-0003', '更新设备下次校准日期', '127.0.0.1', 1, '2026-03-01 10:20:00'),
(4, 1, 'consumable', 'OUTBOUND', 'CON-CHEM-0002', '处理甲醇出库', '127.0.0.1', 1, '2026-03-20 14:05:00'),
(5, 1, 'hazardous_material', 'USAGE', 'HAZ-CHEM-0001', '处理盐酸领用记录', '127.0.0.1', 1, '2026-04-06 10:10:00'),
(8, 1, 'repair', 'COMPLETE', 'repair:6', '完成气相色谱仪维修任务', '127.0.0.1', 1, '2026-05-02 15:40:00'),
(9, 1, 'calibration', 'COMPLETE', 'CAL-2026-ME204E-001', '完成分析天平校准', '127.0.0.1', 1, '2026-01-10 10:30:00');

-- 修正库存预警状态
UPDATE inventory i
LEFT JOIN consumable c
    ON i.item_type = 2
   AND i.item_id = c.id
   AND c.is_deleted = 0
SET i.warning_status = CASE
    WHEN i.expiry_date IS NOT NULL AND i.expiry_date < NOW() THEN 3
    WHEN i.expiry_date IS NOT NULL AND DATEDIFF(i.expiry_date, NOW()) <= 30 THEN 2
    WHEN i.item_type = 2 AND c.id IS NOT NULL AND i.quantity <= c.safety_stock THEN 1
    ELSE 0
END
WHERE i.is_deleted = 0;

UPDATE inventory i
LEFT JOIN hazardous_material hm
    ON i.item_type = 3
   AND i.item_id = hm.id
   AND hm.is_deleted = 0
SET i.warning_status = CASE
    WHEN i.expiry_date IS NOT NULL AND i.expiry_date < NOW() THEN 3
    WHEN i.expiry_date IS NOT NULL AND DATEDIFF(i.expiry_date, NOW()) <= 30 THEN 2
    WHEN i.item_type = 3 AND hm.id IS NOT NULL AND i.quantity <= hm.safety_stock THEN 1
    ELSE i.warning_status
END
WHERE i.is_deleted = 0;

-- 查询一：当前借出未归还的设备清单
SELECT
    e.equipment_code,
    e.equipment_name,
    e.model,
    l.laboratory_name,
    u.real_name AS borrower_name,
    eb.borrow_date,
    eb.due_date,
    eb.purpose
FROM equipment_borrow eb
INNER JOIN equipment e ON eb.equipment_id = e.id
INNER JOIN laboratory l ON eb.laboratory_id = l.id
INNER JOIN user u ON eb.borrower_user_id = u.id
WHERE eb.is_deleted = 0
  AND eb.borrow_status IN (2, 4)
  AND eb.actual_return_date IS NULL
ORDER BY eb.borrow_date DESC
LIMIT 10;

-- 查询二：各类耗材年度领用数量统计
SELECT
    cc.category_name AS consumable_category_name,
    c.consumable_name,
    c.specification,
    c.unit,
    YEAR(co.outbound_date) AS outbound_year,
    SUM(co.quantity) AS annual_total_quantity,
    SUM(co.total_amount) AS annual_total_amount
FROM consumable_outbound co
INNER JOIN consumable c ON co.consumable_id = c.id
INNER JOIN consumable_category cc ON c.category_id = cc.id
WHERE co.is_deleted = 0
  AND co.outbound_status = 2
GROUP BY
    cc.category_name,
    c.consumable_name,
    c.specification,
    c.unit,
    YEAR(co.outbound_date)
ORDER BY outbound_year DESC, annual_total_quantity DESC
LIMIT 10;

-- 查询三：故障/维修次数最多的设备信息
SELECT
    e.equipment_code,
    e.equipment_name,
    e.model,
    e.purchase_date,
    COUNT(er.id) AS repair_count,
    MAX(er.repair_end_time) AS last_repair_date,
    SUM(er.repair_cost) AS total_repair_cost
FROM equipment e
INNER JOIN equipment_repair er ON e.id = er.equipment_id
WHERE e.is_deleted = 0
  AND er.is_deleted = 0
GROUP BY
    e.equipment_code,
    e.equipment_name,
    e.model,
    e.purchase_date
ORDER BY repair_count DESC, last_repair_date DESC
LIMIT 10;

-- 查询四：某实验室近半年入库与出库记录
SELECT
    x.record_type,
    x.item_name,
    x.specification,
    x.quantity,
    x.unit,
    x.unit_price,
    x.total_amount,
    x.operator_name,
    x.operation_date
FROM (
    SELECT
        'INBOUND' AS record_type,
        c.consumable_name AS item_name,
        c.specification,
        ci.quantity,
        c.unit,
        ci.unit_price,
        ci.total_amount,
        u.real_name AS operator_name,
        ci.inbound_date AS operation_date
    FROM consumable_inbound ci
    INNER JOIN consumable c ON ci.consumable_id = c.id
    INNER JOIN user u ON ci.operator_user_id = u.id
    WHERE ci.is_deleted = 0
      AND ci.laboratory_id = 1
      AND ci.inbound_date >= DATE_SUB('2026-05-27 00:00:00', INTERVAL 6 MONTH)

    UNION ALL

    SELECT
        'OUTBOUND' AS record_type,
        c.consumable_name AS item_name,
        c.specification,
        co.quantity,
        c.unit,
        co.unit_price,
        co.total_amount,
        u.real_name AS operator_name,
        co.outbound_date AS operation_date
    FROM consumable_outbound co
    INNER JOIN consumable c ON co.consumable_id = c.id
    INNER JOIN user u ON co.operator_user_id = u.id
    WHERE co.is_deleted = 0
      AND co.laboratory_id = 1
      AND co.outbound_date >= DATE_SUB('2026-05-27 00:00:00', INTERVAL 6 MONTH)
) x
ORDER BY x.operation_date DESC
LIMIT 10;

-- 查询五：即将过期的耗材和危化品清单
SELECT
    item_type_name,
    item_name,
    specification,
    batch_no,
    production_date,
    expiry_date,
    remaining_quantity,
    storage_location,
    manager_name
FROM (
    SELECT
        'CONSUMABLE' AS item_type_name,
        c.consumable_name AS item_name,
        c.specification,
        i.batch_no,
        i.production_date,
        i.expiry_date,
        i.quantity AS remaining_quantity,
        c.storage_location,
        mu.real_name AS manager_name
    FROM inventory i
    INNER JOIN consumable c
        ON i.item_type = 2
       AND i.item_id = c.id
    LEFT JOIN user mu ON c.manager_user_id = mu.id
    WHERE i.is_deleted = 0
      AND c.is_deleted = 0
      AND i.expiry_date IS NOT NULL
      AND i.expiry_date BETWEEN '2026-05-27 00:00:00' AND DATE_ADD('2026-05-27 00:00:00', INTERVAL 90 DAY)

    UNION ALL

    SELECT
        'HAZARDOUS' AS item_type_name,
        hm.material_name AS item_name,
        hm.specification,
        i.batch_no,
        i.production_date,
        i.expiry_date,
        i.quantity AS remaining_quantity,
        hm.storage_location,
        mu.real_name AS manager_name
    FROM inventory i
    INNER JOIN hazardous_material hm
        ON i.item_type = 3
       AND i.item_id = hm.id
    LEFT JOIN user mu ON hm.manager_user_id = mu.id
    WHERE i.is_deleted = 0
      AND hm.is_deleted = 0
      AND i.expiry_date IS NOT NULL
      AND i.expiry_date BETWEEN '2026-05-27 00:00:00' AND DATE_ADD('2026-05-27 00:00:00', INTERVAL 90 DAY)
) expiring_items
ORDER BY expiry_date ASC
LIMIT 10;
