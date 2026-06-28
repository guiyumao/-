package edu.university.lab.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.university.lab.common.constant.Messages;
import edu.university.lab.common.constant.RoleConstants;
import edu.university.lab.common.constant.UserConstants;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.common.service.BaseCrudService;
import edu.university.lab.module.role.entity.Role;
import edu.university.lab.module.role.mapper.RoleMapper;
import edu.university.lab.module.user.dto.CreateUserRequest;
import edu.university.lab.module.user.dto.UpdateUserRequest;
import edu.university.lab.module.user.dto.UserListItem;
import edu.university.lab.module.user.entity.User;
import edu.university.lab.module.user.mapper.UserMapper;
import edu.university.lab.module.user.service.UserService;
import edu.university.lab.module.userrole.entity.UserRole;
import edu.university.lab.module.userrole.mapper.UserRoleMapper;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl extends BaseCrudService<UserMapper, User> implements UserService {

    private static final Map<Integer, String> USER_TYPE_ROLE_MAP = Map.of(
        UserConstants.USER_TYPE_SYS_ADMIN, RoleConstants.SYS_ADMIN,
        UserConstants.USER_TYPE_LAB_DIRECTOR, RoleConstants.LAB_DIRECTOR,
        UserConstants.USER_TYPE_EQUIPMENT_ADMIN, RoleConstants.EQUIPMENT_ADMIN,
        UserConstants.USER_TYPE_CONSUMABLE_ADMIN, RoleConstants.CONSUMABLE_ADMIN,
        UserConstants.USER_TYPE_HAZARDOUS_ADMIN, RoleConstants.HAZARDOUS_ADMIN,
        UserConstants.USER_TYPE_TEACHER, RoleConstants.TEACHER,
        UserConstants.USER_TYPE_STUDENT, RoleConstants.STUDENT,
        UserConstants.USER_TYPE_REPAIR_STAFF, RoleConstants.REPAIR_STAFF,
        UserConstants.USER_TYPE_CALIBRATION_STAFF, RoleConstants.CALIBRATION_STAFF
    );

    private final PasswordEncoder passwordEncoder;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;

    public UserServiceImpl(
        PasswordEncoder passwordEncoder,
        RoleMapper roleMapper,
        UserRoleMapper userRoleMapper
    ) {
        this.passwordEncoder = passwordEncoder;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    public Page<UserListItem> pageQuery(PageQuery query) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
            .orderByDesc(User::getId);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(User::getRealName, query.getKeyword())
                .or()
                .like(User::getUsername, query.getKeyword())
                .or()
                .like(User::getUserNo, query.getKeyword()));
        }

        Page<User> page = pageQuery(query, wrapper);
        List<User> users = page.getRecords();
        if (users.isEmpty()) {
            return new Page<UserListItem>(page.getCurrent(), page.getSize(), page.getTotal());
        }

        List<Integer> userIds = users.stream().map(User::getId).toList();
        List<UserRole> userRoles = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>()
            .in(UserRole::getUserId, userIds));
        Map<Integer, List<UserRole>> userRoleMap = userRoles.stream()
            .collect(Collectors.groupingBy(UserRole::getUserId));

        List<Integer> roleIds = userRoles.stream()
            .map(UserRole::getRoleId)
            .distinct()
            .toList();
        Map<Integer, Role> roleMap = roleIds.isEmpty()
            ? Map.of()
            : roleMapper.selectBatchIds(roleIds).stream()
                .filter(role -> role != null && role.getStatus() != null && role.getStatus() == UserConstants.STATUS_ENABLED)
                .collect(Collectors.toMap(Role::getId, Function.identity()));

        List<UserListItem> records = users.stream()
            .map(user -> toUserListItem(user, userRoleMap.getOrDefault(user.getId(), List.of()), roleMap))
            .toList();

        Page<UserListItem> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(records);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createUser(CreateUserRequest request) {
        User user = new User();
        user.setLaboratoryId(request.getLaboratoryId());
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getInitialPassword()));
        user.setRealName(request.getRealName());
        user.setUserNo(request.getUserNo());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setUserType(request.getUserType());
        user.setStatus(request.getStatus());
        boolean saved = save(user);
        syncRoles(user.getId(), request.getUserType(), request.getRoleIds());
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(Integer userId, UpdateUserRequest request) {
        User user = getById(userId);
        if (user == null) {
            throw new IllegalArgumentException(Messages.USER_NOT_FOUND);
        }
        user.setLaboratoryId(request.getLaboratoryId());
        user.setUsername(request.getUsername());
        user.setRealName(request.getRealName());
        user.setUserNo(request.getUserNo());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setUserType(request.getUserType());
        user.setStatus(request.getStatus());
        boolean updated = updateById(user);
        syncRoles(userId, request.getUserType(), request.getRoleIds());
        return updated;
    }

    @Override
    public boolean resetPassword(Integer userId, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new IllegalArgumentException(Messages.USER_NOT_FOUND);
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        return updateById(user);
    }

    private UserListItem toUserListItem(User user, List<UserRole> userRoles, Map<Integer, Role> roleMap) {
        List<Role> roles = userRoles.stream()
            .map(UserRole::getRoleId)
            .map(roleMap::get)
            .filter(role -> role != null)
            .toList();

        return UserListItem.builder()
            .id(user.getId())
            .laboratoryId(user.getLaboratoryId())
            .username(user.getUsername())
            .realName(user.getRealName())
            .userNo(user.getUserNo())
            .phone(user.getPhone())
            .email(user.getEmail())
            .userType(user.getUserType())
            .status(user.getStatus())
            .roleIds(roles.stream().map(Role::getId).toList())
            .roleCodes(roles.stream().map(Role::getRoleCode).toList())
            .build();
    }

    private void syncRoles(Integer userId, Integer userType, List<Integer> requestedRoleIds) {
        Role primaryRole = findEnabledRoleByUserType(userType);
        LinkedHashSet<Integer> resolvedRoleIds = new LinkedHashSet<>();
        resolvedRoleIds.add(primaryRole.getId());

        if (requestedRoleIds != null && !requestedRoleIds.isEmpty()) {
            List<Role> selectedRoles = roleMapper.selectBatchIds(requestedRoleIds);
            boolean hasInvalidRole = selectedRoles.stream()
                .anyMatch(role -> role == null || role.getStatus() == null || role.getStatus() != UserConstants.STATUS_ENABLED);
            if (hasInvalidRole) {
                throw new IllegalArgumentException(Messages.ROLE_NOT_AVAILABLE);
            }
            selectedRoles.stream()
                .map(Role::getId)
                .forEach(resolvedRoleIds::add);
        }

        userRoleMapper.deleteForceByUserId(userId);
        for (Integer roleId : new ArrayList<>(resolvedRoleIds)) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
    }

    private Role findEnabledRoleByUserType(Integer userType) {
        String roleCode = USER_TYPE_ROLE_MAP.get(userType);
        if (!StringUtils.hasText(roleCode)) {
            throw new IllegalArgumentException(Messages.ROLE_NOT_AVAILABLE);
        }
        Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
            .eq(Role::getRoleCode, roleCode)
            .eq(Role::getStatus, UserConstants.STATUS_ENABLED)
            .last("LIMIT 1"));
        if (role == null) {
            throw new IllegalArgumentException(Messages.ROLE_NOT_AVAILABLE);
        }
        return role;
    }
}
