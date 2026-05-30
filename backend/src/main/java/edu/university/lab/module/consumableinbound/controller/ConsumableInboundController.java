package edu.university.lab.module.consumableinbound.controller;

import edu.university.lab.common.api.ApiResponse;
import edu.university.lab.common.api.PageResponse;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.module.consumableinbound.entity.ConsumableInbound;
import edu.university.lab.module.consumableinbound.service.ConsumableInboundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Consumable Inbound")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/consumable-inbounds")
public class ConsumableInboundController {

    private final ConsumableInboundService consumableInboundService;

    @Operation(summary = "分页查询耗材入库记录")
    @PreAuthorize("hasAuthority('consumable_inbound:view')")
    @GetMapping
    public ApiResponse<PageResponse<ConsumableInbound>> page(@Valid PageQuery query) {
        return ApiResponse.success(PageResponse.from(consumableInboundService.pageQuery(query)));
    }

    @Operation(summary = "创建耗材入库业务")
    @PreAuthorize("hasAuthority('consumable_inbound:edit')")
    @PostMapping
    public ApiResponse<ConsumableInbound> create(@RequestBody ConsumableInbound inbound) {
        return ApiResponse.success(consumableInboundService.createInbound(inbound));
    }
}
