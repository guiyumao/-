package edu.university.lab.module.notification.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.university.lab.module.notification.entity.UserNotification;
import java.util.List;

public interface UserNotificationService extends IService<UserNotification> {

    List<UserNotification> listMine();
}
