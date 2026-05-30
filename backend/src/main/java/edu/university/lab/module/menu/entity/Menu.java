package edu.university.lab.module.menu.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.university.lab.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("menu")
public class Menu extends BaseEntity {

    @TableId
    private Integer id;

    private Integer parentId;

    private String menuCode;

    private String menuName;

    private String routePath;

    private String componentName;

    private String permissionCode;

    private Integer menuType;

    private Integer sortOrder;

    private Integer visible;

    private Integer status;

    private String icon;

    private String remarks;
}
