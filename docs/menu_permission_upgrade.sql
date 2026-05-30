USE lab_management_system;

SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM role_menu;
DELETE FROM menu;
SET FOREIGN_KEY_CHECKS = 1;

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
  AND m.id IN (1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36);

INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id FROM role r INNER JOIN menu m
WHERE r.role_code = 'equipment_admin'
  AND m.id IN (1,6,7,8,12,13,14,15,19,25,26,27,28,29,36);

INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id FROM role r INNER JOIN menu m
WHERE r.role_code = 'consumable_admin'
  AND m.id IN (1,6,9,10,12,16,17,19,20,30,31,32,33,36);

INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id FROM role r INNER JOIN menu m
WHERE r.role_code = 'hazardous_admin'
  AND m.id IN (1,6,11,12,18,19,20,34,35,36);

INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id FROM role r INNER JOIN menu m
WHERE r.role_code = 'teacher'
  AND m.id IN (1,6,8,10,11,12,13,14,17,18,19,27,28,33,35,36);

INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id FROM role r INNER JOIN menu m
WHERE r.role_code = 'student'
  AND m.id IN (1,6,8,10,12,13,17,27,33);

INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id FROM role r INNER JOIN menu m
WHERE r.role_code = 'repair_staff'
  AND m.id IN (1,6,8,12,14,19,28,36);

INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id FROM role r INNER JOIN menu m
WHERE r.role_code = 'calibration_staff'
  AND m.id IN (1,6,8,12,15,19,29,36);
