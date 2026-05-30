package edu.university.lab.module.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsResponse {

    private long laboratoryCount;

    private long equipmentCount;

    private long consumableCount;

    private long hazardousCount;

    private long currentBorrowCount;

    private BigDecimal monthlyConsumableAmount;

    private long pendingCalibrations;

    private long expiringConsumables;

    private List<SimpleChartItem> consumableTrend;
}
