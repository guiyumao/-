package edu.university.lab.module.equipmentcategory.controller;

import edu.university.lab.common.api.ApiResponse;
import edu.university.lab.common.api.PageResponse;
import edu.university.lab.common.constant.Messages;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.module.equipmentcategory.entity.EquipmentCategory;
import edu.university.lab.module.equipmentcategory.service.EquipmentCategoryService;
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

@Tag(name = "设备分类")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/equipment-categories")
public class EquipmentCategoryController {

    private final EquipmentCategoryService equipmentCategoryService;

    @Operation(summary = "分页查询设备分类")
    @PreAuthorize("hasAuthority('equipment_category:view')")
    @GetMapping
    public ApiResponse<PageResponse<EquipmentCategory>> page(@Valid PageQuery query) {
        return ApiResponse.success(PageResponse.from(equipmentCategoryService.pageQuery(query)));
    }

    @Operation(summary = "获取设备分类详情")
    @PreAuthorize("hasAuthority('equipment_category:view')")
    @GetMapping("/{id}")
    public ApiResponse<EquipmentCategory> getById(@PathVariable Integer id) {
        return ApiResponse.success(equipmentCategoryService.getById(id));
    }

    @Operation(summary = "新增设备分类")
    @PreAuthorize("hasAuthority('equipment_category:edit')")
    @PostMapping
    public ApiResponse<Boolean> create(@RequestBody EquipmentCategory entity) {
        return ApiResponse.success(Messages.EQUIPMENT_CATEGORY_CREATED, equipmentCategoryService.save(entity));
    }

    @Operation(summary = "更新设备分类")
    @PreAuthorize("hasAuthority('equipment_category:edit')")
    @PutMapping("/{id}")
    public ApiResponse<Boolean> update(@PathVariable Integer id, @RequestBody EquipmentCategory entity) {
        entity.setId(id);
        return ApiResponse.success(Messages.EQUIPMENT_CATEGORY_UPDATED, equipmentCategoryService.updateById(entity));
    }

    @Operation(summary = "删除设备分类")
    @PreAuthorize("hasAuthority('equipment_category:edit')")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Integer id) {
        return ApiResponse.success(Messages.EQUIPMENT_CATEGORY_DELETED, equipmentCategoryService.removeById(id));
    }
}
