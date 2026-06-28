package edu.university.lab.module.hazardousmaterial.controller;

import edu.university.lab.common.api.ApiResponse;
import edu.university.lab.common.api.PageResponse;
import edu.university.lab.common.constant.Messages;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.module.hazardousmaterial.entity.HazardousMaterial;
import edu.university.lab.module.hazardousmaterial.service.HazardousMaterialService;
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

@Tag(name = "危化品")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hazardous-materials")
public class HazardousMaterialController {

    private final HazardousMaterialService hazardousMaterialService;

    @Operation(summary = "分页查询危化品")
    @PreAuthorize("hasAuthority('hazardous_material:view')")
    @GetMapping
    public ApiResponse<PageResponse<HazardousMaterial>> page(@Valid PageQuery query) {
        return ApiResponse.success(PageResponse.from(hazardousMaterialService.pageQuery(query)));
    }

    @Operation(summary = "获取危化品详情")
    @PreAuthorize("hasAuthority('hazardous_material:view')")
    @GetMapping("/{id}")
    public ApiResponse<HazardousMaterial> getById(@PathVariable Integer id) {
        return ApiResponse.success(hazardousMaterialService.getById(id));
    }

    @Operation(summary = "新增危化品")
    @PreAuthorize("hasAuthority('hazardous_material:edit')")
    @PostMapping
    public ApiResponse<Boolean> create(@RequestBody HazardousMaterial entity) {
        return ApiResponse.success(Messages.HAZARDOUS_CREATED, hazardousMaterialService.save(entity));
    }

    @Operation(summary = "更新危化品")
    @PreAuthorize("hasAuthority('hazardous_material:edit')")
    @PutMapping("/{id}")
    public ApiResponse<Boolean> update(@PathVariable Integer id, @RequestBody HazardousMaterial entity) {
        entity.setId(id);
        return ApiResponse.success(Messages.HAZARDOUS_UPDATED, hazardousMaterialService.updateById(entity));
    }

    @Operation(summary = "删除危化品")
    @PreAuthorize("hasAuthority('hazardous_material:edit')")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Integer id) {
        return ApiResponse.success(Messages.HAZARDOUS_DELETED, hazardousMaterialService.removeById(id));
    }
}
