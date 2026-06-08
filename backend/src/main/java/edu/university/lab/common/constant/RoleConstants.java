package edu.university.lab.common.constant;

import java.util.Set;

public final class RoleConstants {

    public static final String SYS_ADMIN = "sys_admin";

    public static final String LAB_DIRECTOR = "lab_director";

    public static final String TEACHER = "teacher";

    public static final String STUDENT = "student";

    public static final Set<String> LAB_SCOPE_BYPASS_ROLES = Set.of(SYS_ADMIN, LAB_DIRECTOR);

    private RoleConstants() {
    }
}
