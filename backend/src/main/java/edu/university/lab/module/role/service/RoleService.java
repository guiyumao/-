package edu.university.lab.module.role.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.university.lab.module.role.dto.RoleSaveRequest;
import edu.university.lab.module.role.entity.Role;
import java.util.List;

public interface RoleService extends IService<Role> {

    Role createRole(RoleSaveRequest request);

    Role updateRole(Integer id, RoleSaveRequest request);

    List<Integer> menuIds(Integer roleId);

    boolean assignMenus(Integer roleId, List<Integer> menuIds);
}
