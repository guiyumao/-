package edu.university.lab.module.equipmentborrow.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EquipmentBorrowReminderRequest {

    @Size(max = 500, message = "message cannot exceed 500 characters")
    private String message;
}
