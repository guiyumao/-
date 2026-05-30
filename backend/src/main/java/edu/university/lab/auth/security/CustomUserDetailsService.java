package edu.university.lab.auth.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.university.lab.module.role.entity.Role;
import edu.university.lab.module.role.mapper.RoleMapper;
import edu.university.lab.module.rolemenu.entity.RoleMenu;
import edu.university.lab.module.rolemenu.mapper.RoleMenuMapper;
import edu.university.lab.module.menu.entity.Menu;
import edu.university.lab.module.menu.mapper.MenuMapper;
import edu.university.lab.module.user.entity.User;
import edu.university.lab.module.user.mapper.UserMapper;
import edu.university.lab.module.userrole.entity.UserRole;
import edu.university.lab.module.userrole.mapper.UserRoleMapper;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 用户详情服务
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final MenuMapper menuMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
            .eq(User::getUsername, username)
            .last("LIMIT 1"));
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        List<UserRole> userRoles = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>()
            .eq(UserRole::getUserId, user.getId()));
        List<String> roleCodes = userRoles.isEmpty()
            ? Collections.emptyList()
            : userRoles.stream()
                .map(UserRole::getRoleId)
                .map(roleMapper::selectById)
                .filter(role -> role != null && role.getStatus() != null && role.getStatus() == 1)
                .map(Role::getRoleCode)
                .toList();
        List<String> permissionCodes = userRoles.isEmpty()
            ? Collections.emptyList()
            : userRoles.stream()
                .map(UserRole::getRoleId)
                .flatMap(roleId -> roleMenuMapper.selectList(new LambdaQueryWrapper<RoleMenu>()
                    .eq(RoleMenu::getRoleId, roleId))
                    .stream())
                .map(RoleMenu::getMenuId)
                .distinct()
                .map(menuMapper::selectById)
                .filter(menu -> menu != null
                    && menu.getStatus() != null
                    && menu.getStatus() == 1)
                .map(Menu::getPermissionCode)
                .filter(Objects::nonNull)
                .filter(permission -> !permission.isBlank())
                .distinct()
                .toList();
        return new LoginUser(user, roleCodes, permissionCodes);
    }
}
