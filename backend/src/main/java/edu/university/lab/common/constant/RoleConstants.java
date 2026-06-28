package edu.university.lab.common.constant;

import java.util.Set;

public final class RoleConstants {

    public static final String SYS_ADMIN = "sys_admin";

    public static final String LAB_DIRECTOR = "lab_director";

    public static final String EQUIPMENT_ADMIN = "equipment_admin";

    public static final String CONSUMABLE_ADMIN = "consumable_admin";

    public static final String HAZARDOUS_ADMIN = "hazardous_admin";

    public static final String TEACHER = "teacher";

    public static final String STUDENT = "student";

    public static final String REPAIR_STAFF = "repair_staff";

    public static final String CALIBRATION_STAFF = "calibration_staff";

    public static final Set<String> LAB_SCOPE_BYPASS_ROLES = Set.of(SYS_ADMIN, LAB_DIRECTOR);

    private RoleConstants() {
    }
}
