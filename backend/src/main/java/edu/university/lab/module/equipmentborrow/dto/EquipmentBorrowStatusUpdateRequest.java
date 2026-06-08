package edu.university.lab.module.equipmentborrow.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EquipmentBorrowStatusUpdateRequest {

    @NotNull(message = "borrowStatus is required")
    @Min(value = 1, message = "borrowStatus must be between 1 and 5")
    @Max(value = 5, message = "borrowStatus must be between 1 and 5")
    private Integer borrowStatus;

    private String returnCondition;

    private String remarks;
}
