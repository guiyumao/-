package edu.university.lab.module.laboratory.controller;

import edu.university.lab.common.api.ApiResponse;
import edu.university.lab.common.api.PageResponse;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.module.laboratory.entity.Laboratory;
import edu.university.lab.module.laboratory.service.LaboratoryService;
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

@Tag(name = "Laboratory")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/laboratories")
public class LaboratoryController {

    private final LaboratoryService laboratoryService;

    @Operation(summary = "分页查询实验室")
    @PreAuthorize("hasAuthority('laboratory:view')")
    @GetMapping
    public ApiResponse<PageResponse<Laboratory>> page(@Valid PageQuery query) {
        return ApiResponse.success(PageResponse.from(laboratoryService.pageQuery(query)));
    }

    @Operation(summary = "获取实验室详情")
    @PreAuthorize("hasAuthority('laboratory:view')")
    @GetMapping("/{id}")
    public ApiResponse<Laboratory> getById(@PathVariable Integer id) {
        return ApiResponse.success(laboratoryService.getById(id));
    }

    @Operation(summary = "新增实验室")
    @PreAuthorize("hasAuthority('laboratory:edit')")
    @PostMapping
    public ApiResponse<Boolean> create(@RequestBody Laboratory laboratory) {
        return ApiResponse.success("created", laboratoryService.save(laboratory));
    }

    @Operation(summary = "更新实验室")
    @PreAuthorize("hasAuthority('laboratory:edit')")
    @PutMapping("/{id}")
    public ApiResponse<Boolean> update(@PathVariable Integer id, @RequestBody Laboratory laboratory) {
        laboratory.setId(id);
        return ApiResponse.success("updated", laboratoryService.updateById(laboratory));
    }

    @Operation(summary = "删除实验室")
    @PreAuthorize("hasAuthority('laboratory:edit')")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Integer id) {
        return ApiResponse.success("deleted", laboratoryService.removeById(id));
    }
}
