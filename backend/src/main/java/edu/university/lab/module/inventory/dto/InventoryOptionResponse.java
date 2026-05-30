package edu.university.lab.module.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * 库存选项响应
 */
@Data
@Builder
public class InventoryOptionResponse {

    private Integer id;

    private Integer laboratoryId;

    private Integer itemType;

    private Integer itemId;

    private String batchNo;

    private BigDecimal quantity;

    private BigDecimal lockedQuantity;

    private BigDecimal availableQuantity;

    private BigDecimal unitPrice;

    private LocalDateTime productionDate;

    private LocalDateTime expiryDate;

    private Integer warningStatus;
}
