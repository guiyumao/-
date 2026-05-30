package edu.university.lab.module.reminder.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReminderResponse {

    private List<ReminderItem> pendingCalibrations;

    private List<ReminderItem> expiringConsumables;
}
