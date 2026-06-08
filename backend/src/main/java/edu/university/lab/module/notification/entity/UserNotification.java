package edu.university.lab.module.notification.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.university.lab.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_notification")
public class UserNotification extends BaseEntity {

    @TableId
    private Integer id;

    private Integer receiverUserId;

    private Integer senderUserId;

    private String title;

    private String content;

    private String notificationType;

    private String relatedType;

    private Integer relatedId;

    private Integer readStatus;
}
