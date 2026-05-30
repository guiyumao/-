package edu.university.lab.module.hazardoususage.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.university.lab.common.entity.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 危化品使用流水实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hazardous_material_usage")
public class HazardousUsage extends BaseEntity {

    @TableId
    private Integer id;

    private Integer hazardousMaterialId;

    private Integer laboratoryId;

    private Integer applicantUserId;

    private Integer approverUserId;

    private Integer operatorUserId;

    private Integer actionType;

    private String batchNo;

    private BigDecimal quantity;

    private BigDecimal remainingQuantity;

    private LocalDateTime usageDate;

    private String purpose;

    private String projectName;

    private String witnessName;

    private Integer usageStatus;

    private String remarks;
}
