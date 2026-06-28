package edu.university.lab.common.constant;

/**
 * 系统消息常量。
 * 所有面向用户的提示统一收敛到这里，避免英文提示和分散硬编码。
 */
public final class Messages {

    private Messages() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    public static final String SUCCESS = "操作成功";
    public static final String VALIDATION_FAILED = "请求参数校验失败";
    public static final String SYSTEM_ERROR = "系统异常，请稍后重试";

    public static final String LOGIN_SUCCESS = "登录成功";
    public static final String REGISTER_SUCCESS = "注册成功";
    public static final String USER_NOT_FOUND = "用户不存在";
    public static final String USERNAME_PASSWORD_ERROR = "用户名或密码错误";
    public static final String INIT_ADMIN_CONFIG_REQUIRED = "启用初始化管理员时，必须填写管理员账号和密码";
    public static final String INIT_ADMIN_ROLE_MISSING = "缺少系统管理员角色，无法完成初始化";

    public static final String USERNAME_EXISTS = "用户名已存在";
    public static final String USER_NO_EXISTS = "工号或学号已存在";
    public static final String PHONE_EXISTS = "手机号已存在";
    public static final String EMAIL_EXISTS = "邮箱已存在";
    public static final String LABORATORY_UNAVAILABLE = "实验室不可用";
    public static final String ROLE_NOT_AVAILABLE = "当前注册角色不可用";

    public static final String LABORATORY_CREATED = "实验室创建成功";
    public static final String LABORATORY_UPDATED = "实验室更新成功";
    public static final String LABORATORY_DELETED = "实验室删除成功";

    public static final String USER_CREATED = "用户创建成功";
    public static final String USER_UPDATED = "用户更新成功";
    public static final String USER_DELETED = "用户删除成功";
    public static final String PASSWORD_RESET_SUCCESS = "密码重置成功";

    public static final String EQUIPMENT_CREATED = "设备创建成功";
    public static final String EQUIPMENT_UPDATED = "设备更新成功";
    public static final String EQUIPMENT_DELETED = "设备删除成功";
    public static final String EQUIPMENT_NOT_FOUND = "设备不存在";
    public static final String EQUIPMENT_NOT_AVAILABLE = "设备当前不可借用";

    public static final String EQUIPMENT_CATEGORY_CREATED = "设备分类创建成功";
    public static final String EQUIPMENT_CATEGORY_UPDATED = "设备分类更新成功";
    public static final String EQUIPMENT_CATEGORY_DELETED = "设备分类删除成功";

    public static final String CONSUMABLE_CREATED = "耗材创建成功";
    public static final String CONSUMABLE_UPDATED = "耗材更新成功";
    public static final String CONSUMABLE_DELETED = "耗材删除成功";
    public static final String CONSUMABLE_CATEGORY_CREATED = "耗材分类创建成功";
    public static final String CONSUMABLE_CATEGORY_UPDATED = "耗材分类更新成功";
    public static final String CONSUMABLE_CATEGORY_DELETED = "耗材分类删除成功";
    public static final String INSUFFICIENT_INVENTORY = "库存不足，无法完成出库";

    public static final String HAZARDOUS_CREATED = "危化品创建成功";
    public static final String HAZARDOUS_UPDATED = "危化品更新成功";
    public static final String HAZARDOUS_DELETED = "危化品删除成功";
    public static final String INSUFFICIENT_HAZARDOUS_INVENTORY = "危化品库存不足，无法完成领用";

    public static final String BORROW_NOT_FOUND = "借用记录不存在";
    public static final String BORROW_DELETED = "借用记录删除成功";
    public static final String BORROW_RETURNED = "归还登记成功";
    public static final String BORROW_REMINDER_SENT = "催还提醒发送成功";
    public static final String ALREADY_RETURNED = "该设备已归还";
    public static final String DUE_DATE_INVALID = "应还时间必须晚于借用时间";
    public static final String ONLY_OVERDUE_CAN_REMIND = "只有逾期未还的记录才能发送催还提醒";

    public static final String REPAIR_NOT_FOUND = "维修记录不存在";
    public static final String CALIBRATION_NOT_FOUND = "校准记录不存在";
    public static final String CALIBRATION_DATE_INVALID = "校准有效期必须晚于校准时间";

    public static final String ROLE_CREATED = "角色创建成功";
    public static final String ROLE_UPDATED = "角色更新成功";
    public static final String ROLE_NOT_FOUND = "角色不存在";
    public static final String ROLE_CODE_EXISTS = "角色编码已存在";
    public static final String MENU_ASSIGNED = "角色菜单分配成功";
}
