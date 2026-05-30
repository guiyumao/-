package edu.university.lab.module.report.controller;

import edu.university.lab.common.api.ApiResponse;
import edu.university.lab.common.api.PageResponse;
import edu.university.lab.module.report.dto.ReportQuery;
import edu.university.lab.module.report.dto.ReportRow;
import edu.university.lab.module.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Report")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "获取报表汇总列表")
    @PreAuthorize("hasAuthority('report:view')")
    @GetMapping("/summary")
    public ApiResponse<PageResponse<ReportRow>> summary(@Valid ReportQuery query) {
        return ApiResponse.success(PageResponse.from(reportService.summaryRows(query)));
    }

    @Operation(summary = "导出报表 CSV")
    @PreAuthorize("hasAuthority('report:export')")
    @GetMapping("/summary/export")
    public ResponseEntity<byte[]> export(@Valid ReportQuery query) {
        query.setCurrent(1);
        query.setPageSize(1000);
        byte[] content = reportService.exportCsv(query);
        String fileName = new String("lab-report-summary.csv".getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(content);
    }
}
