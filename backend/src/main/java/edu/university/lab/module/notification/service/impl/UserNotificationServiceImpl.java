package edu.university.lab.module.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.university.lab.auth.security.LoginUser;
import edu.university.lab.auth.security.SecurityUtils;
import edu.university.lab.module.notification.entity.UserNotification;
import edu.university.lab.module.notification.mapper.UserNotificationMapper;
import edu.university.lab.module.notification.service.UserNotificationService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UserNotificationServiceImpl extends ServiceImpl<UserNotificationMapper, UserNotification> implements UserNotificationService {

    @Override
    public List<UserNotification> listMine() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || loginUser.getUser() == null) {
            return List.of();
        }
        return list(new LambdaQueryWrapper<UserNotification>()
            .eq(UserNotification::getReceiverUserId, loginUser.getUser().getId())
            .orderByAsc(UserNotification::getReadStatus)
            .orderByDesc(UserNotification::getCreateTime));
    }
}
