package edu.university.lab.module.report.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportRow {

    private String reportType;

    private String itemCode;

    private String itemName;

    private String laboratoryName;

    private Integer laboratoryId;

    private String businessDate;

    private String quantity;

    private String amount;

    private String statusText;
}
