package edu.university.lab.module.consumablecategory.controller;

import edu.university.lab.common.api.ApiResponse;
import edu.university.lab.common.api.PageResponse;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.module.consumablecategory.entity.ConsumableCategory;
import edu.university.lab.module.consumablecategory.service.ConsumableCategoryService;
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

@Tag(name = "Consumable Category")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/consumable-categories")
public class ConsumableCategoryController {

    private final ConsumableCategoryService consumableCategoryService;

    @Operation(summary = "Page consumable categories")
    @PreAuthorize("hasAuthority('consumable_category:view')")
    @GetMapping
    public ApiResponse<PageResponse<ConsumableCategory>> page(@Valid PageQuery query) {
        return ApiResponse.success(PageResponse.from(consumableCategoryService.pageQuery(query)));
    }

    @Operation(summary = "Get consumable category by id")
    @PreAuthorize("hasAuthority('consumable_category:view')")
    @GetMapping("/{id}")
    public ApiResponse<ConsumableCategory> getById(@PathVariable Integer id) {
        return ApiResponse.success(consumableCategoryService.getById(id));
    }

    @Operation(summary = "Create consumable category")
    @PreAuthorize("hasAuthority('consumable_category:edit')")
    @PostMapping
    public ApiResponse<Boolean> create(@RequestBody ConsumableCategory entity) {
        return ApiResponse.success("created", consumableCategoryService.save(entity));
    }

    @Operation(summary = "Update consumable category")
    @PreAuthorize("hasAuthority('consumable_category:edit')")
    @PutMapping("/{id}")
    public ApiResponse<Boolean> update(@PathVariable Integer id, @RequestBody ConsumableCategory entity) {
        entity.setId(id);
        return ApiResponse.success("updated", consumableCategoryService.updateById(entity));
    }

    @Operation(summary = "Delete consumable category")
    @PreAuthorize("hasAuthority('consumable_category:edit')")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Integer id) {
        return ApiResponse.success("deleted", consumableCategoryService.removeById(id));
    }
}
