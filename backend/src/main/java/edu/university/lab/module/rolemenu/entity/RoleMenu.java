package edu.university.lab.module.rolemenu.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.university.lab.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色菜单关联实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("role_menu")
public class RoleMenu extends BaseEntity {

    @TableId
    private Integer id;

    private Integer roleId;

    private Integer menuId;
}
