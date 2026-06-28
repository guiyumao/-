package edu.university.lab.module.role.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.university.lab.common.api.ApiResponse;
import edu.university.lab.common.constant.Messages;
import edu.university.lab.module.role.dto.RoleMenuAssignRequest;
import edu.university.lab.module.role.dto.RoleSaveRequest;
import edu.university.lab.module.role.entity.Role;
import edu.university.lab.module.role.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "角色")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "获取角色列表")
    @PreAuthorize("hasAuthority('role:view')")
    @GetMapping
    public ApiResponse<List<Role>> list() {
        return ApiResponse.success(roleService.list(new LambdaQueryWrapper<Role>()
            .eq(Role::getStatus, 1)
            .orderByAsc(Role::getId)));
    }

    @Operation(summary = "新增角色")
    @PreAuthorize("hasAuthority('role:edit')")
    @PostMapping
    public ApiResponse<Role> create(@Valid @RequestBody RoleSaveRequest request) {
        return ApiResponse.success(roleService.createRole(request));
    }

    @Operation(summary = "更新角色")
    @PreAuthorize("hasAuthority('role:edit')")
    @PutMapping("/{id}")
    public ApiResponse<Role> update(@PathVariable Integer id, @Valid @RequestBody RoleSaveRequest request) {
        return ApiResponse.success(roleService.updateRole(id, request));
    }

    @Operation(summary = "获取角色菜单ID列表")
    @PreAuthorize("hasAuthority('role:view')")
    @GetMapping("/{id}/menu-ids")
    public ApiResponse<List<Integer>> menuIds(@PathVariable Integer id) {
        return ApiResponse.success(roleService.menuIds(id));
    }

    @Operation(summary = "分配角色菜单")
    @PreAuthorize("hasAuthority('role:edit')")
    @PostMapping("/{id}/menus")
    public ApiResponse<Boolean> assignMenus(@PathVariable Integer id, @RequestBody RoleMenuAssignRequest request) {
        return ApiResponse.success(Messages.MENU_ASSIGNED, roleService.assignMenus(id, request.getMenuIds()));
    }
}
