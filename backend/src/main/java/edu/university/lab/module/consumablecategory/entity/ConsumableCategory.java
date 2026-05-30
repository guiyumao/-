package edu.university.lab.module.consumablecategory.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.university.lab.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 耗材类别实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("consumable_category")
public class ConsumableCategory extends BaseEntity {

    @TableId
    private Integer id;

    private String categoryCode;

    private String categoryName;

    private String description;

    private Integer status;
}
