package edu.university.lab.module.dashboard.controller;

import edu.university.lab.common.api.ApiResponse;
import edu.university.lab.module.dashboard.dto.DashboardStatsResponse;
import edu.university.lab.module.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "获取仪表盘统计数据")
    @PreAuthorize("hasAuthority('dashboard:view')")
    @GetMapping("/stats")
    public ApiResponse<DashboardStatsResponse> stats() {
        return ApiResponse.success(dashboardService.stats());
    }
}
