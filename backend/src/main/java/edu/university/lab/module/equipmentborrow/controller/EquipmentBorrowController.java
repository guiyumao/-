package edu.university.lab.module.equipmentborrow.controller;

import edu.university.lab.common.api.ApiResponse;
import edu.university.lab.common.api.PageResponse;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.module.equipmentborrow.dto.EquipmentReturnRequest;
import edu.university.lab.module.equipmentborrow.entity.EquipmentBorrow;
import edu.university.lab.module.equipmentborrow.service.EquipmentBorrowBusinessService;
import edu.university.lab.module.equipmentborrow.service.EquipmentBorrowService;
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

@Tag(name = "Equipment Borrow")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/equipment-borrows")
public class EquipmentBorrowController {

    private final EquipmentBorrowService equipmentBorrowService;

    private final EquipmentBorrowBusinessService equipmentBorrowBusinessService;

    @Operation(summary = "分页查询设备借用记录")
    @PreAuthorize("hasAuthority('equipment_borrow:view')")
    @GetMapping
    public ApiResponse<PageResponse<EquipmentBorrow>> page(@Valid PageQuery query) {
        return ApiResponse.success(PageResponse.from(equipmentBorrowService.pageQuery(query)));
    }

    @Operation(summary = "获取设备借用详情")
    @PreAuthorize("hasAuthority('equipment_borrow:view')")
    @GetMapping("/{id}")
    public ApiResponse<EquipmentBorrow> getById(@PathVariable Integer id) {
        return ApiResponse.success(equipmentBorrowService.getById(id));
    }

    @Operation(summary = "提交设备借用")
    @PreAuthorize("hasAuthority('equipment_borrow:edit')")
    @PostMapping
    public ApiResponse<EquipmentBorrow> create(@RequestBody EquipmentBorrow entity) {
        return ApiResponse.success(equipmentBorrowBusinessService.borrow(entity));
    }

    @Operation(summary = "设备归还")
    @PreAuthorize("hasAuthority('equipment_borrow:edit')")
    @PutMapping("/{id}/return")
    public ApiResponse<Boolean> returnEquipment(@PathVariable Integer id, @Valid @RequestBody EquipmentReturnRequest request) {
        return ApiResponse.success("returned", equipmentBorrowBusinessService.returnEquipment(id, request.getReturnCondition(), request.getRemarks()));
    }

    @Operation(summary = "删除设备借用记录")
    @PreAuthorize("hasAuthority('equipment_borrow:edit')")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Integer id) {
        return ApiResponse.success("deleted", equipmentBorrowService.removeById(id));
    }
}
