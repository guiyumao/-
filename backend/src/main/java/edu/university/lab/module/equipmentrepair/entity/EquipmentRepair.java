package edu.university.lab.module.equipmentrepair.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.university.lab.common.entity.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备维修实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("equipment_repair")
public class EquipmentRepair extends BaseEntity {

    @TableId
    private Integer id;

    private Integer equipmentId;

    private Integer laboratoryId;

    private Integer reporterUserId;

    private Integer repairUserId;

    private String faultDescription;

    private LocalDateTime reportTime;

    private LocalDateTime repairStartTime;

    private LocalDateTime repairEndTime;

    private Integer repairStatus;

    private BigDecimal repairCost;

    private String repairResult;

    private String remarks;
}
