package edu.university.lab.module.role.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.university.lab.module.role.dto.RoleSaveRequest;
import edu.university.lab.module.role.entity.Role;
import edu.university.lab.module.role.mapper.RoleMapper;
import edu.university.lab.module.role.service.RoleService;
import edu.university.lab.module.rolemenu.entity.RoleMenu;
import edu.university.lab.module.rolemenu.mapper.RoleMenuMapper;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RoleMenuMapper roleMenuMapper;

    @Override
    public Role createRole(RoleSaveRequest request) {
        long exists = count(new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, request.getRoleCode()));
        if (exists > 0) {
            throw new IllegalArgumentException("Role code already exists");
        }
        Role role = new Role();
        role.setRoleCode(request.getRoleCode());
        role.setRoleName(request.getRoleName());
        role.setStatus(1);
        save(role);
        return role;
    }

    @Override
    public Role updateRole(Integer id, RoleSaveRequest request) {
        Role role = getById(id);
        if (role == null) {
            throw new IllegalArgumentException("Role not found");
        }
        role.setRoleName(request.getRoleName());
        updateById(role);
        return role;
    }

    @Override
    public List<Integer> menuIds(Integer roleId) {
        if (roleId == null) {
            return Collections.emptyList();
        }
        return roleMenuMapper.selectList(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, roleId))
            .stream()
            .map(RoleMenu::getMenuId)
            .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignMenus(Integer roleId, List<Integer> menuIds) {
        roleMenuMapper.deleteForceByRoleId(roleId);
        if (menuIds == null || menuIds.isEmpty()) {
            return true;
        }
        menuIds.stream()
            .distinct()
            .forEach(menuId -> {
                RoleMenu roleMenu = new RoleMenu();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                roleMenuMapper.insert(roleMenu);
            });
        return true;
    }
}
