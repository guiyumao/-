package edu.university.lab.module.equipmentcategory.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.university.lab.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备类别实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("equipment_category")
public class EquipmentCategory extends BaseEntity {

    @TableId
    private Integer id;

    private String categoryCode;

    private String categoryName;

    private String description;

    private Integer status;
}
