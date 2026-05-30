package edu.university.lab.module.inventory.controller;

import edu.university.lab.common.api.ApiResponse;
import edu.university.lab.module.inventory.dto.InventoryOptionResponse;
import edu.university.lab.module.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Inventory")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "查询业务可用库存批次")
    @PreAuthorize("hasAuthority('inventory:view')")
    @GetMapping("/options")
    public ApiResponse<List<InventoryOptionResponse>> options(
        @RequestParam Integer laboratoryId,
        @RequestParam Integer itemType,
        @RequestParam Integer itemId
    ) {
        return ApiResponse.success(inventoryService.listAvailableOptions(laboratoryId, itemType, itemId));
    }
}
