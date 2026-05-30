-- 实验室设备与耗材管理系统增量迁移
USE lab_management_system;

CREATE TABLE IF NOT EXISTS sys_audit_log (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id INT NULL COMMENT '用户ID',
    username VARCHAR(64) NULL COMMENT '用户名',
    operation VARCHAR(128) NOT NULL COMMENT '操作描述',
    method VARCHAR(255) NOT NULL COMMENT '请求方法',
    params TEXT NULL COMMENT '请求参数',
    ip VARCHAR(64) NULL COMMENT '请求IP',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_sys_audit_log_user_id (user_id),
    INDEX idx_sys_audit_log_create_time (create_time)
) COMMENT='操作审计日志表';

CREATE OR REPLACE VIEW v_equipment_borrow_status AS
SELECT
    e.id AS equipment_id,
    e.equipment_name,
    e.model,
    l.laboratory_name,
    eb.borrow_status,
    u.real_name AS borrower_name,
    eb.due_date,
    eb.actual_return_date
FROM equipment e
LEFT JOIN laboratory l ON l.id = e.laboratory_id AND l.is_deleted = 0
LEFT JOIN equipment_borrow eb ON eb.equipment_id = e.id AND eb.is_deleted = 0
LEFT JOIN user u ON u.id = eb.borrower_user_id AND u.is_deleted = 0
WHERE e.is_deleted = 0;

DROP PROCEDURE IF EXISTS sp_consumable_yearly_stats;
DELIMITER $$
CREATE PROCEDURE sp_consumable_yearly_stats(IN p_year INT)
BEGIN
    SELECT
        cc.category_name,
        c.consumable_name,
        c.unit,
        SUM(co.quantity) AS total_quantity
    FROM consumable_outbound co
    INNER JOIN consumable c ON c.id = co.consumable_id AND c.is_deleted = 0
    INNER JOIN consumable_category cc ON cc.id = c.category_id AND cc.is_deleted = 0
    WHERE co.is_deleted = 0
      AND YEAR(co.outbound_date) = p_year
    GROUP BY cc.category_name, c.consumable_name, c.unit
    ORDER BY cc.category_name, c.consumable_name;
END $$
DELIMITER ;

DROP TRIGGER IF EXISTS trg_after_borrow_return;
DELIMITER $$
CREATE TRIGGER trg_after_borrow_return
AFTER UPDATE ON equipment_borrow
FOR EACH ROW
BEGIN
    IF NEW.borrow_status = 3 AND NEW.actual_return_date IS NOT NULL AND (OLD.borrow_status IS NULL OR OLD.borrow_status <> 3) THEN
        UPDATE equipment
        SET status = 1, update_time = NOW()
        WHERE id = NEW.equipment_id;

        INSERT INTO sys_audit_log (user_id, username, operation, method, params, ip, create_time)
        VALUES (
            NEW.borrower_user_id,
            'trigger',
            '触发器归还设备',
            'TRIGGER equipment_borrow',
            CONCAT('borrow_id=', NEW.id),
            '127.0.0.1',
            NOW()
        );
    END IF;
END $$
DELIMITER ;

INSERT INTO menu (
    id, parent_id, menu_code, menu_name, route_path, component_name, permission_code,
    menu_type, sort_order, visible, status, icon, remarks
)
SELECT 37, NULL, 'inventory_group', '库存管理', '/inventory', NULL, 'inventory:view', 1, 47, 1, 1, 'Box', 'inventory group'
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE id = 37);

INSERT INTO menu (
    id, parent_id, menu_code, menu_name, route_path, component_name, permission_code,
    menu_type, sort_order, visible, status, icon, remarks
)
SELECT 38, 37, 'inventory_alert', '库存预警', '/inventory/alert', 'InventoryAlertView', 'inventory:view', 1, 48, 1, 1, 'Warning', 'inventory alert'
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE id = 38);

INSERT INTO menu (
    id, parent_id, menu_code, menu_name, route_path, component_name, permission_code,
    menu_type, sort_order, visible, status, icon, remarks
)
SELECT 39, 2, 'audit_log', '操作日志', '/audit-logs', 'AuditLogView', 'audit:view', 1, 24, 1, 1, 'Document', 'audit log'
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE id = 39);

INSERT INTO menu (
    id, parent_id, menu_code, menu_name, route_path, component_name, permission_code,
    menu_type, sort_order, visible, status, icon, remarks
)
SELECT 40, NULL, 'reminder', '到期提醒', '/reminders', 'ReminderView', 'reminder:view', 1, 15, 1, 1, 'Bell', 'reminder page'
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE id = 40);

INSERT INTO menu (
    id, parent_id, menu_code, menu_name, route_path, component_name, permission_code,
    menu_type, sort_order, visible, status, icon, remarks
)
SELECT 41, NULL, 'audit_view', '/audit-logs view', '/audit-logs', NULL, 'audit:view', 2, 1145, 0, 1, NULL, 'audit permission'
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE id = 41);

INSERT INTO menu (
    id, parent_id, menu_code, menu_name, route_path, component_name, permission_code,
    menu_type, sort_order, visible, status, icon, remarks
)
SELECT 42, NULL, 'reminder_view', '/reminders view', '/reminders', NULL, 'reminder:view', 2, 1146, 0, 1, NULL, 'reminder permission'
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE id = 42);

INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM role r
INNER JOIN menu m ON m.id IN (37, 38, 39, 40, 41, 42)
WHERE r.role_code = 'sys_admin'
  AND NOT EXISTS (
      SELECT 1 FROM role_menu rm WHERE rm.role_id = r.id AND rm.menu_id = m.id AND rm.is_deleted = 0
  );

INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM role r
INNER JOIN menu m ON m.id IN (37, 38, 39, 40, 41, 42)
WHERE r.role_code = 'lab_director'
  AND NOT EXISTS (
      SELECT 1 FROM role_menu rm WHERE rm.role_id = r.id AND rm.menu_id = m.id AND rm.is_deleted = 0
  );
