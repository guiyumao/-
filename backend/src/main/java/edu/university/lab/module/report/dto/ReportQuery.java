package edu.university.lab.module.report.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class ReportQuery {

    @Min(1)
    private long current = 1;

    @Min(1)
    @Max(200)
    private long pageSize = 10;

    private Integer labId;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;

    /**
     * 业务类型：borrow/repair/calibration/consumable/hazardous
     */
    private String type;
}
