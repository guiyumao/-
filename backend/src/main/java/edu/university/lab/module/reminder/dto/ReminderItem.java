package edu.university.lab.module.reminder.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReminderItem {

    private String reminderType;

    private String itemName;

    private String laboratoryName;

    private String batchNo;

    private LocalDateTime dueDate;

    private BigDecimal remainingQuantity;
}
