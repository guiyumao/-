package edu.university.lab.module.equipmentborrow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 设备归还请求
 */
@Data
public class EquipmentReturnRequest {

    @NotBlank(message = "returnCondition is required")
    private String returnCondition;

    private String remarks;
}
