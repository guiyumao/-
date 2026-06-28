package edu.university.lab.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.university.lab.auth.dto.AuthContextResponse;
import edu.university.lab.auth.dto.LoginRequest;
import edu.university.lab.auth.dto.LoginResponse;
import edu.university.lab.auth.dto.MenuItem;
import edu.university.lab.auth.dto.RegisterRequest;
import edu.university.lab.auth.dto.RegisterResponse;
import edu.university.lab.auth.dto.UserProfile;
import edu.university.lab.auth.security.CustomUserDetailsService;
import edu.university.lab.auth.security.JwtTokenProvider;
import edu.university.lab.auth.security.LoginUser;
import edu.university.lab.auth.security.SecurityUtils;
import edu.university.lab.auth.service.AuthService;
import edu.university.lab.common.constant.Messages;
import edu.university.lab.common.constant.RoleConstants;
import edu.university.lab.common.constant.UserConstants;
import edu.university.lab.module.laboratory.entity.Laboratory;
import edu.university.lab.module.laboratory.mapper.LaboratoryMapper;
import edu.university.lab.module.menu.entity.Menu;
import edu.university.lab.module.menu.mapper.MenuMapper;
import edu.university.lab.module.role.entity.Role;
import edu.university.lab.module.role.mapper.RoleMapper;
import edu.university.lab.module.rolemenu.entity.RoleMenu;
import edu.university.lab.module.rolemenu.mapper.RoleMenuMapper;
import edu.university.lab.module.user.entity.User;
import edu.university.lab.module.user.mapper.UserMapper;
import edu.university.lab.module.userrole.entity.UserRole;
import edu.university.lab.module.userrole.mapper.UserRoleMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    private final UserMapper userMapper;
    private final LaboratoryMapper laboratoryMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        LoginUser loginUser = customUserDetailsService.loadUserByUsernameAndRole(
            request.getUsername(),
            request.getRoleCode()
        );
        String token = jwtTokenProvider.createToken(loginUser);
        return LoginResponse.builder()
            .token(token)
            .expireMinutes(jwtTokenProvider.getExpireMinutes())
            .user(toProfile(loginUser))
            .roleCode(loginUser.getActiveRoleCode())
            .menus(buildMenus(List.of(loginUser.getActiveRoleCode())))
            .permissions(loginUser.getPermissionCodes())
            .build();
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        validateRegisterRequest(request);

        String roleCode = resolveRoleCode(request.getRegisterType());
        Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
            .eq(Role::getRoleCode, roleCode)
            .eq(Role::getStatus, UserConstants.STATUS_ENABLED)
            .last("LIMIT 1"));
        if (role == null) {
            throw new IllegalArgumentException(Messages.ROLE_NOT_AVAILABLE);
        }

        User user = new User();
        user.setLaboratoryId(request.getLaboratoryId());
        user.setUsername(request.getUsername().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName().trim());
        user.setUserNo(request.getUserNo().trim());
        user.setPhone(normalize(request.getPhone()));
        user.setEmail(normalize(request.getEmail()));
        user.setUserType(resolveUserType(request.getRegisterType()));
        user.setStatus(UserConstants.STATUS_ENABLED);
        userMapper.insert(user);

        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(role.getId());
        userRoleMapper.insert(userRole);

        return RegisterResponse.builder()
            .userId(user.getId())
            .username(user.getUsername())
            .realName(user.getRealName())
            .roleCode(roleCode)
            .message(Messages.REGISTER_SUCCESS)
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
            .roleCode(loginUser.getActiveRoleCode())
            .menus(buildMenus(List.of(loginUser.getActiveRoleCode())))
            .permissions(loginUser.getPermissionCodes())
            .build();
    }

    public List<Role> availableRoles(String username) {
        if (!StringUtils.hasText(username)) {
            return roleMapper.selectList(new LambdaQueryWrapper<Role>()
                .eq(Role::getStatus, UserConstants.STATUS_ENABLED)
                .orderByAsc(Role::getId));
        }

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
            .eq(User::getUsername, username)
            .last("LIMIT 1"));
        if (user == null) {
            return List.of();
        }

        List<Integer> roleIds = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, user.getId()))
            .stream()
            .map(UserRole::getRoleId)
            .distinct()
            .toList();

        return roleIds.isEmpty()
            ? List.of()
            : roleIds.stream()
                .map(roleMapper::selectById)
                .filter(role -> role != null && role.getStatus() != null && role.getStatus() == UserConstants.STATUS_ENABLED)
                .toList();
    }

    private void validateRegisterRequest(RegisterRequest request) {
        Laboratory laboratory = laboratoryMapper.selectById(request.getLaboratoryId());
        if (laboratory == null || laboratory.getStatus() == null || laboratory.getStatus() != UserConstants.STATUS_ENABLED) {
            throw new IllegalArgumentException(Messages.LABORATORY_UNAVAILABLE);
        }
        if (existsUser(User::getUsername, request.getUsername().trim())) {
            throw new IllegalArgumentException(Messages.USERNAME_EXISTS);
        }
        if (existsUser(User::getUserNo, request.getUserNo().trim())) {
            throw new IllegalArgumentException(Messages.USER_NO_EXISTS);
        }
        if (StringUtils.hasText(request.getPhone()) && existsUser(User::getPhone, request.getPhone().trim())) {
            throw new IllegalArgumentException(Messages.PHONE_EXISTS);
        }
        if (StringUtils.hasText(request.getEmail()) && existsUser(User::getEmail, request.getEmail().trim())) {
            throw new IllegalArgumentException(Messages.EMAIL_EXISTS);
        }
    }

    private <T> boolean existsUser(com.baomidou.mybatisplus.core.toolkit.support.SFunction<User, T> column, T value) {
        return userMapper.selectCount(new LambdaQueryWrapper<User>().eq(column, value)) > 0;
    }

    private String resolveRoleCode(String registerType) {
        return "student".equals(registerType) ? RoleConstants.STUDENT : RoleConstants.TEACHER;
    }

    private int resolveUserType(String registerType) {
        return "student".equals(registerType) ? UserConstants.USER_TYPE_STUDENT : UserConstants.USER_TYPE_TEACHER;
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
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
