package edu.university.lab.module.notification.controller;

import edu.university.lab.common.api.ApiResponse;
import edu.university.lab.module.notification.entity.UserNotification;
import edu.university.lab.module.notification.service.UserNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Notification")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class UserNotificationController {

    private final UserNotificationService userNotificationService;

    @Operation(summary = "查询我的通知")
    @GetMapping("/mine")
    public ApiResponse<List<UserNotification>> mine() {
        return ApiResponse.success(userNotificationService.listMine());
    }
}
