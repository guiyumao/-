package edu.university.lab.module.dashboard.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SimpleChartItem {

    private String label;

    private BigDecimal value;
}
