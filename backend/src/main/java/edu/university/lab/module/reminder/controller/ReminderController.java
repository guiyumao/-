package edu.university.lab.module.reminder.controller;

import edu.university.lab.common.api.ApiResponse;
import edu.university.lab.module.reminder.dto.ReminderResponse;
import edu.university.lab.module.reminder.service.ReminderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Reminder")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    @Operation(summary = "查询到期提醒")
    @GetMapping
    public ApiResponse<ReminderResponse> list() {
        return ApiResponse.success(reminderService.listReminders());
    }
}
