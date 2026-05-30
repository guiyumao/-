package edu.university.lab.module.user.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.university.lab.common.entity.BaseEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {

    @TableId
    private Integer id;

    private Integer laboratoryId;

    private String username;

    private String passwordHash;

    private String realName;

    private String userNo;

    private Integer gender;

    private String phone;

    private String email;

    private Integer userType;

    private Integer status;

    private LocalDateTime lastLoginTime;
}
