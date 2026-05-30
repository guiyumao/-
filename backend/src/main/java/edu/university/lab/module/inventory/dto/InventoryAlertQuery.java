package edu.university.lab.module.inventory.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class InventoryAlertQuery {

    @Min(1)
    private long current = 1;

    @Min(1)
    @Max(100)
    private long pageSize = 10;

    private String keyword;
}
