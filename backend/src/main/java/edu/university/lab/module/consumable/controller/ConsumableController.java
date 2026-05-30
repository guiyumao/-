package edu.university.lab.module.consumable.controller;

import edu.university.lab.common.api.ApiResponse;
import edu.university.lab.common.api.PageResponse;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.module.consumable.entity.Consumable;
import edu.university.lab.module.consumable.service.ConsumableService;
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

@Tag(name = "Consumable")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/consumables")
public class ConsumableController {

    private final ConsumableService consumableService;

    @Operation(summary = "Page consumables")
    @PreAuthorize("hasAuthority('consumable:view')")
    @GetMapping
    public ApiResponse<PageResponse<Consumable>> page(@Valid PageQuery query) {
        return ApiResponse.success(PageResponse.from(consumableService.pageQuery(query)));
    }

    @Operation(summary = "Get consumable by id")
    @PreAuthorize("hasAuthority('consumable:view')")
    @GetMapping("/{id}")
    public ApiResponse<Consumable> getById(@PathVariable Integer id) {
        return ApiResponse.success(consumableService.getById(id));
    }

    @Operation(summary = "Create consumable")
    @PreAuthorize("hasAuthority('consumable:edit')")
    @PostMapping
    public ApiResponse<Boolean> create(@RequestBody Consumable entity) {
        return ApiResponse.success("created", consumableService.save(entity));
    }

    @Operation(summary = "Update consumable")
    @PreAuthorize("hasAuthority('consumable:edit')")
    @PutMapping("/{id}")
    public ApiResponse<Boolean> update(@PathVariable Integer id, @RequestBody Consumable entity) {
        entity.setId(id);
        return ApiResponse.success("updated", consumableService.updateById(entity));
    }

    @Operation(summary = "Delete consumable")
    @PreAuthorize("hasAuthority('consumable:edit')")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Integer id) {
        return ApiResponse.success("deleted", consumableService.removeById(id));
    }
}
