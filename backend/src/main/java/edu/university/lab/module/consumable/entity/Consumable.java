package edu.university.lab.module.consumable.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.university.lab.common.entity.BaseEntity;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 耗材实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("consumable")
public class Consumable extends BaseEntity {

    @TableId
    private Integer id;

    private Integer laboratoryId;

    private Integer categoryId;

    private Integer managerUserId;

    private String consumableCode;

    private String consumableName;

    private String specification;

    private String unit;

    private BigDecimal unitPrice;

    private BigDecimal safetyStock;

    private BigDecimal maxStock;

    private String storageLocation;

    private Integer expiryRequired;

    private Integer status;

    private String remarks;
}
