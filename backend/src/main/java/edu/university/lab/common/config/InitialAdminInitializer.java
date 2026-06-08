package edu.university.lab.common.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.university.lab.common.constant.RoleConstants;
import edu.university.lab.common.constant.UserConstants;
import edu.university.lab.module.role.entity.Role;
import edu.university.lab.module.role.mapper.RoleMapper;
import edu.university.lab.module.user.entity.User;
import edu.university.lab.module.user.mapper.UserMapper;
import edu.university.lab.module.userrole.entity.UserRole;
import edu.university.lab.module.userrole.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class InitialAdminInitializer implements ApplicationRunner {

    private final InitialAdminProperties properties;

    private final UserMapper userMapper;

    private final RoleMapper roleMapper;

    private final UserRoleMapper userRoleMapper;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled()) {
            return;
        }
        if (!StringUtils.hasText(properties.getUsername()) || !StringUtils.hasText(properties.getPassword())) {
            throw new IllegalStateException("Initial admin username and password are required when initialization is enabled");
        }
        String username = properties.getUsername().trim();
        boolean exists = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) > 0;
        if (exists) {
            return;
        }

        Role adminRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
            .eq(Role::getRoleCode, RoleConstants.SYS_ADMIN)
            .last("LIMIT 1"));
        if (adminRole == null) {
            throw new IllegalStateException("System administrator role is missing");
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(properties.getPassword()));
        user.setRealName(resolveOrDefault(properties.getRealName(), "系统管理员"));
        user.setUserNo(resolveOrDefault(properties.getUserNo(), username));
        user.setGender(0);
        user.setUserType(UserConstants.USER_TYPE_SYS_ADMIN);
        user.setStatus(UserConstants.STATUS_ENABLED);
        userMapper.insert(user);

        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(adminRole.getId());
        userRoleMapper.insert(userRole);
    }

    private String resolveOrDefault(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }
}
