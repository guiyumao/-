-- Multi-role permission alignment for the laboratory management design document.
USE lab_management_system;

INSERT INTO role (role_code, role_name, description, status)
SELECT 'equipment_admin', 'Equipment Administrator', '设备管理员', 1
WHERE NOT EXISTS (SELECT 1 FROM role WHERE role_code = 'equipment_admin');

INSERT INTO role (role_code, role_name, description, status)
SELECT 'consumable_admin', 'Consumable Administrator', '耗材管理员', 1
WHERE NOT EXISTS (SELECT 1 FROM role WHERE role_code = 'consumable_admin');

INSERT INTO role (role_code, role_name, description, status)
SELECT 'hazardous_admin', 'Hazardous Material Administrator', '危化品管理员', 1
WHERE NOT EXISTS (SELECT 1 FROM role WHERE role_code = 'hazardous_admin');

INSERT INTO role (role_code, role_name, description, status)
SELECT 'repair_staff', 'Repair Staff', '维修人员', 1
WHERE NOT EXISTS (SELECT 1 FROM role WHERE role_code = 'repair_staff');

INSERT INTO role (role_code, role_name, description, status)
SELECT 'calibration_staff', 'Calibration Staff', '校准人员', 1
WHERE NOT EXISTS (SELECT 1 FROM role WHERE role_code = 'calibration_staff');

UPDATE role
SET status = 1
WHERE role_code IN (
    'equipment_admin',
    'consumable_admin',
    'hazardous_admin',
    'repair_staff',
    'calibration_staff'
);

INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM role r
INNER JOIN menu m ON m.menu_code IN (
    'dashboard', 'asset_group', 'equipment_category', 'equipment', 'business_group',
    'equipment_borrow', 'equipment_repair', 'equipment_calibration', 'report_center',
    'inventory_group', 'inventory_alert', 'reminder', 'equipment_category_edit',
    'equipment_edit', 'equipment_borrow_edit', 'equipment_repair_edit',
    'equipment_calibration_edit', 'inventory_option', 'reminder_view', 'report_export'
)
WHERE r.role_code = 'equipment_admin'
  AND NOT EXISTS (
      SELECT 1 FROM role_menu rm WHERE rm.role_id = r.id AND rm.menu_id = m.id AND rm.is_deleted = 0
  );

INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM role r
INNER JOIN menu m ON m.menu_code IN (
    'dashboard', 'asset_group', 'consumable_category', 'consumable', 'business_group',
    'consumable_inbound', 'consumable_outbound', 'report_center', 'inventory_group',
    'inventory_alert', 'reminder', 'inventory_option', 'consumable_category_edit',
    'consumable_edit', 'consumable_inbound_edit', 'consumable_outbound_edit',
    'reminder_view', 'report_export'
)
WHERE r.role_code = 'consumable_admin'
  AND NOT EXISTS (
      SELECT 1 FROM role_menu rm WHERE rm.role_id = r.id AND rm.menu_id = m.id AND rm.is_deleted = 0
  );

INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM role r
INNER JOIN menu m ON m.menu_code IN (
    'dashboard', 'asset_group', 'hazardous_material', 'business_group',
    'hazardous_usage', 'report_center', 'inventory_group', 'inventory_alert',
    'reminder', 'inventory_option', 'hazardous_material_edit',
    'hazardous_usage_edit', 'reminder_view', 'report_export'
)
WHERE r.role_code = 'hazardous_admin'
  AND NOT EXISTS (
      SELECT 1 FROM role_menu rm WHERE rm.role_id = r.id AND rm.menu_id = m.id AND rm.is_deleted = 0
  );

INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM role r
INNER JOIN menu m ON m.menu_code IN (
    'dashboard', 'asset_group', 'equipment', 'business_group',
    'equipment_repair', 'reminder', 'equipment_repair_edit', 'reminder_view'
)
WHERE r.role_code = 'repair_staff'
  AND NOT EXISTS (
      SELECT 1 FROM role_menu rm WHERE rm.role_id = r.id AND rm.menu_id = m.id AND rm.is_deleted = 0
  );

INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM role r
INNER JOIN menu m ON m.menu_code IN (
    'dashboard', 'asset_group', 'equipment', 'business_group',
    'equipment_calibration', 'reminder', 'equipment_calibration_edit', 'reminder_view'
)
WHERE r.role_code = 'calibration_staff'
  AND NOT EXISTS (
      SELECT 1 FROM role_menu rm WHERE rm.role_id = r.id AND rm.menu_id = m.id AND rm.is_deleted = 0
  );
