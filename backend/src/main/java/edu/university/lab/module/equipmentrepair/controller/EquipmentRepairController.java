package edu.university.lab.module.equipmentrepair.controller;

import edu.university.lab.common.api.ApiResponse;
import edu.university.lab.common.api.PageResponse;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.module.equipmentrepair.entity.EquipmentRepair;
import edu.university.lab.module.equipmentrepair.service.EquipmentRepairService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Equipment Repair")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/equipment-repairs")
public class EquipmentRepairController {

    private final EquipmentRepairService equipmentRepairService;

    @Operation(summary = "分页查询设备维修记录")
    @PreAuthorize("hasAuthority('equipment_repair:view')")
    @GetMapping
    public ApiResponse<PageResponse<EquipmentRepair>> page(@Valid PageQuery query) {
        return ApiResponse.success(PageResponse.from(equipmentRepairService.pageQuery(query)));
    }

    @Operation(summary = "创建设备报修")
    @PreAuthorize("hasAuthority('equipment_repair:edit')")
    @PostMapping
    public ApiResponse<EquipmentRepair> create(@RequestBody EquipmentRepair request) {
        return ApiResponse.success(equipmentRepairService.createRepair(request));
    }

    @Operation(summary = "更新维修状态")
    @PreAuthorize("hasAuthority('equipment_repair:edit')")
    @PutMapping("/{id}/status")
    public ApiResponse<EquipmentRepair> updateStatus(@PathVariable Integer id, @RequestBody EquipmentRepair request) {
        return ApiResponse.success(equipmentRepairService.updateStatus(id, request));
    }

    @Operation(summary = "删除设备维修记录")
    @PreAuthorize("hasAuthority('equipment_repair:edit')")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Integer id) {
        return ApiResponse.success(equipmentRepairService.removeById(id));
    }
}
