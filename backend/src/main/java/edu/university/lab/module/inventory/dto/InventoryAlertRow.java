package edu.university.lab.module.inventory.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InventoryAlertRow {

    private Integer consumableId;

    private String consumableCode;

    private String consumableName;

    private BigDecimal currentStock;

    private BigDecimal safetyStock;

    private String storageLocation;
}
