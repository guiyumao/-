package edu.university.lab.module.inventory.controller;

import edu.university.lab.common.api.ApiResponse;
import edu.university.lab.common.api.PageResponse;
import edu.university.lab.module.inventory.dto.InventoryAlertQuery;
import edu.university.lab.module.inventory.dto.InventoryAlertRow;
import edu.university.lab.module.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Inventory Alert")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inventory")
public class InventoryAlertController {

    private final InventoryService inventoryService;

    @Operation(summary = "分页查询库存预警")
    @PreAuthorize("hasAuthority('inventory:view')")
    @GetMapping("/alert")
    public ApiResponse<PageResponse<InventoryAlertRow>> page(@Valid InventoryAlertQuery query) {
        return ApiResponse.success(PageResponse.from(inventoryService.pageAlerts(query)));
    }
}
