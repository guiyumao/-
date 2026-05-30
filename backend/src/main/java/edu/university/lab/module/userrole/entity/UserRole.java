package edu.university.lab.module.userrole.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.university.lab.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户角色关联实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_role")
public class UserRole extends BaseEntity {

    @TableId
    private Integer id;

    private Integer userId;

    private Integer roleId;
}
