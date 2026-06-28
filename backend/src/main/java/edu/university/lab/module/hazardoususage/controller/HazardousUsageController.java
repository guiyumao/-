package edu.university.lab.module.hazardoususage.controller;

import edu.university.lab.common.api.ApiResponse;
import edu.university.lab.common.api.PageResponse;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.module.hazardoususage.entity.HazardousUsage;
import edu.university.lab.module.hazardoususage.service.HazardousUsageService;
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

@Tag(name = "Hazardous Usage")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hazardous-usages")
public class HazardousUsageController {

    private final HazardousUsageService hazardousUsageService;

    @Operation(summary = "分页查询危化品业务记录")
    @PreAuthorize("hasAuthority('hazardous_usage:view')")
    @GetMapping
    public ApiResponse<PageResponse<HazardousUsage>> page(@Valid PageQuery query) {
        return ApiResponse.success(PageResponse.from(hazardousUsageService.pageQuery(query)));
    }

    @Operation(summary = "创建危化品业务记录")
    @PreAuthorize("hasAuthority('hazardous_usage:edit')")
    @PostMapping
    public ApiResponse<HazardousUsage> create(@RequestBody HazardousUsage usage) {
        return ApiResponse.success(hazardousUsageService.createUsage(usage));
    }

    @Operation(summary = "删除危化品使用记录")
    @PreAuthorize("hasAuthority('hazardous_usage:edit')")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Integer id) {
        return ApiResponse.success(hazardousUsageService.removeById(id));
    }
}
