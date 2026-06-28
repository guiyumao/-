package edu.university.lab.module.equipment.controller;

import edu.university.lab.common.api.ApiResponse;
import edu.university.lab.common.api.PageResponse;
import edu.university.lab.common.constant.Messages;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.module.equipment.entity.Equipment;
import edu.university.lab.module.equipment.service.EquipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Equipment")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;

    @Operation(summary = "分页查询设备")
    @PreAuthorize("hasAuthority('equipment:view')")
    @GetMapping
    public ApiResponse<PageResponse<Equipment>> page(@Valid PageQuery query) {
        return ApiResponse.success(PageResponse.from(equipmentService.pageQuery(query)));
    }

    @Operation(summary = "获取设备详情")
    @PreAuthorize("hasAuthority('equipment:view')")
    @GetMapping("/{id}")
    public ApiResponse<Equipment> getById(@PathVariable Integer id) {
        return ApiResponse.success(equipmentService.getById(id));
    }

    @Operation(summary = "新增设备")
    @PreAuthorize("hasAuthority('equipment:edit')")
    @PostMapping
    public ApiResponse<Boolean> create(@RequestBody Equipment entity) {
        return ApiResponse.success(Messages.EQUIPMENT_CREATED, equipmentService.save(entity));
    }

    @Operation(summary = "更新设备")
    @PreAuthorize("hasAuthority('equipment:edit')")
    @PutMapping("/{id}")
    public ApiResponse<Boolean> update(@PathVariable Integer id, @RequestBody Equipment entity) {
        entity.setId(id);
        return ApiResponse.success(Messages.EQUIPMENT_UPDATED, equipmentService.updateById(entity));
    }

    @Operation(summary = "删除设备")
    @PreAuthorize("hasAuthority('equipment:edit')")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Integer id) {
        return ApiResponse.success(Messages.EQUIPMENT_DELETED, equipmentService.removeById(id));
    }
}
