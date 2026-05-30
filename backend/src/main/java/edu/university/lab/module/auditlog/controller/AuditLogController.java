package edu.university.lab.module.auditlog.controller;

import edu.university.lab.common.api.ApiResponse;
import edu.university.lab.common.api.PageResponse;
import edu.university.lab.module.auditlog.dto.AuditLogQuery;
import edu.university.lab.module.auditlog.entity.SysAuditLog;
import edu.university.lab.module.auditlog.service.SysAuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Audit Log")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final SysAuditLogService sysAuditLogService;

    @Operation(summary = "分页查询操作审计日志")
    @PreAuthorize("hasAuthority('audit:view')")
    @GetMapping
    public ApiResponse<PageResponse<SysAuditLog>> page(@Valid AuditLogQuery query) {
        return ApiResponse.success(PageResponse.from(sysAuditLogService.pageQuery(query)));
    }
}
