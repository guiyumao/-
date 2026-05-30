package edu.university.lab.auth.service.impl;

import edu.university.lab.auth.dto.AuthContextResponse;
import edu.university.lab.auth.dto.LoginRequest;
import edu.university.lab.auth.dto.LoginResponse;
import edu.university.lab.auth.dto.MenuItem;
import edu.university.lab.auth.dto.UserProfile;
import edu.university.lab.auth.security.CustomUserDetailsService;
import edu.university.lab.auth.security.JwtTokenProvider;
import edu.university.lab.auth.security.LoginUser;
import edu.university.lab.auth.security.SecurityUtils;
import edu.university.lab.auth.service.AuthService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.university.lab.module.menu.entity.Menu;
import edu.university.lab.module.menu.mapper.MenuMapper;
import edu.university.lab.module.role.entity.Role;
import edu.university.lab.module.role.mapper.RoleMapper;
import edu.university.lab.module.rolemenu.entity.RoleMenu;
import edu.university.lab.module.rolemenu.mapper.RoleMenuMapper;
import edu.university.lab.module.userrole.entity.UserRole;
import edu.university.lab.module.userrole.mapper.UserRoleMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;

    private final CustomUserDetailsService customUserDetailsService;

    private final JwtTokenProvider jwtTokenProvider;

    private final UserRoleMapper userRoleMapper;

    private final RoleMapper roleMapper;

    private final RoleMenuMapper roleMenuMapper;

    private final MenuMapper menuMapper;

    @Override
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        LoginUser loginUser = (LoginUser) customUserDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtTokenProvider.createToken(loginUser);
        return LoginResponse.builder()
            .token(token)
            .expireMinutes(jwtTokenProvider.getExpireMinutes())
            .user(toProfile(loginUser))
            .menus(buildMenus(loginUser.getRoleCodes()))
            .permissions(loginUser.getPermissionCodes())
            .build();
    }

    @Override
    public UserProfile currentUser() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        return loginUser == null ? null : toProfile(loginUser);
    }

    @Override
    public AuthContextResponse currentContext() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            return null;
        }
        return AuthContextResponse.builder()
            .user(toProfile(loginUser))
            .menus(buildMenus(loginUser.getRoleCodes()))
            .permissions(loginUser.getPermissionCodes())
            .build();
    }

    private UserProfile toProfile(LoginUser loginUser) {
        return UserProfile.builder()
            .id(loginUser.getUser().getId())
            .laboratoryId(loginUser.getUser().getLaboratoryId())
            .username(loginUser.getUser().getUsername())
            .realName(loginUser.getUser().getRealName())
            .userType(loginUser.getUser().getUserType())
            .roleCodes(loginUser.getRoleCodes())
            .build();
    }

    private List<MenuItem> buildMenus(List<String> roleCodes) {
        List<Integer> roleIds = roleCodes.stream()
            .map(roleCode -> roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleCode, roleCode)
                .last("LIMIT 1")))
            .filter(role -> role != null && role.getStatus() != null && role.getStatus() == 1)
            .map(Role::getId)
            .toList();

        Map<Integer, Menu> menuMap = new LinkedHashMap<>();
        roleIds.stream()
            .flatMap(roleId -> roleMenuMapper.selectList(new LambdaQueryWrapper<RoleMenu>()
                .eq(RoleMenu::getRoleId, roleId))
                .stream())
            .map(RoleMenu::getMenuId)
            .distinct()
            .map(menuMapper::selectById)
            .filter(menu -> menu != null
                && menu.getStatus() != null
                && menu.getStatus() == 1
                && menu.getVisible() != null
                && menu.getVisible() == 1
                && menu.getMenuType() != null
                && menu.getMenuType() == 1)
            .forEach(menu -> collectMenuWithParents(menu, menuMap));

        return new ArrayList<>(menuMap.values()).stream()
            .sorted((left, right) -> Integer.compare(
                left.getSortOrder() == null ? Integer.MAX_VALUE : left.getSortOrder(),
                right.getSortOrder() == null ? Integer.MAX_VALUE : right.getSortOrder()
            ))
            .map(menu -> MenuItem.builder()
                .id(menu.getId())
                .parentId(menu.getParentId())
                .path(menu.getRoutePath())
                .label(menu.getMenuName())
                .permission(menu.getPermissionCode())
                .component(menu.getComponentName())
                .sortOrder(menu.getSortOrder())
                .icon(menu.getIcon())
                .build())
            .toList();
    }

    private void collectMenuWithParents(Menu menu, Map<Integer, Menu> menuMap) {
        if (menu == null || menu.getId() == null || menuMap.containsKey(menu.getId())) {
            return;
        }
        if (menu.getParentId() != null && menu.getParentId() != 0) {
            Menu parent = menuMapper.selectById(menu.getParentId());
            if (parent != null
                && parent.getStatus() != null
                && parent.getStatus() == 1
                && parent.getVisible() != null
                && parent.getVisible() == 1
                && parent.getMenuType() != null
                && parent.getMenuType() == 1) {
                collectMenuWithParents(parent, menuMap);
            }
        }
        menuMap.put(menu.getId(), menu);
    }
}
