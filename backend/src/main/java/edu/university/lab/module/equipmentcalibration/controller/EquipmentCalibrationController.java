package edu.university.lab.module.equipmentcalibration.controller;

import edu.university.lab.common.api.ApiResponse;
import edu.university.lab.common.api.PageResponse;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.module.equipmentcalibration.entity.EquipmentCalibration;
import edu.university.lab.module.equipmentcalibration.service.EquipmentCalibrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Equipment Calibration")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/equipment-calibrations")
public class EquipmentCalibrationController {

    private final EquipmentCalibrationService equipmentCalibrationService;

    @Operation(summary = "分页查询设备校准记录")
    @PreAuthorize("hasAuthority('equipment_calibration:view')")
    @GetMapping
    public ApiResponse<PageResponse<EquipmentCalibration>> page(@Valid PageQuery query) {
        return ApiResponse.success(PageResponse.from(equipmentCalibrationService.pageQuery(query)));
    }

    @Operation(summary = "创建设备校准任务")
    @PreAuthorize("hasAuthority('equipment_calibration:edit')")
    @PostMapping
    public ApiResponse<EquipmentCalibration> create(@RequestBody EquipmentCalibration request) {
        return ApiResponse.success(equipmentCalibrationService.createCalibration(request));
    }

    @Operation(summary = "确认设备校准结果")
    @PreAuthorize("hasAuthority('equipment_calibration:edit')")
    @PutMapping("/{id}/confirm")
    public ApiResponse<EquipmentCalibration> confirm(@PathVariable Integer id, @RequestBody EquipmentCalibration request) {
        return ApiResponse.success(equipmentCalibrationService.confirmCalibration(id, request));
    }
}
