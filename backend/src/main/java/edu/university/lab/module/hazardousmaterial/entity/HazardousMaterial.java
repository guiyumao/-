package edu.university.lab.module.hazardousmaterial.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.university.lab.common.entity.BaseEntity;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 危化品实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hazardous_material")
public class HazardousMaterial extends BaseEntity {

    @TableId
    private Integer id;

    private Integer laboratoryId;

    private Integer managerUserId;

    private String hazardousCode;

    private String materialName;

    private String casNo;

    private String hazardCategory;

    private String specification;

    private String unit;

    private String concentration;

    private String storageLocation;

    private BigDecimal safetyStock;

    private String msdsCode;

    private Integer status;

    private String remarks;
}
