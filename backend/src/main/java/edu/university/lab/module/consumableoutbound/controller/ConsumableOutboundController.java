package edu.university.lab.module.consumableoutbound.controller;

import edu.university.lab.common.api.ApiResponse;
import edu.university.lab.common.api.PageResponse;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.module.consumableoutbound.entity.ConsumableOutbound;
import edu.university.lab.module.consumableoutbound.service.ConsumableOutboundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Consumable Outbound")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/consumable-outbounds")
public class ConsumableOutboundController {

    private final ConsumableOutboundService consumableOutboundService;

    @Operation(summary = "分页查询耗材出库记录")
    @PreAuthorize("hasAuthority('consumable_outbound:view')")
    @GetMapping
    public ApiResponse<PageResponse<ConsumableOutbound>> page(@Valid PageQuery query) {
        return ApiResponse.success(PageResponse.from(consumableOutboundService.pageQuery(query)));
    }

    @Operation(summary = "创建耗材出库业务")
    @PreAuthorize("hasAuthority('consumable_outbound:edit')")
    @PostMapping
    public ApiResponse<ConsumableOutbound> create(@RequestBody ConsumableOutbound outbound) {
        return ApiResponse.success(consumableOutboundService.createOutbound(outbound));
    }

    @Operation(summary = "删除耗材出库记录")
    @PreAuthorize("hasAuthority('consumable_outbound:edit')")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Integer id) {
        return ApiResponse.success(consumableOutboundService.removeById(id));
    }
}
