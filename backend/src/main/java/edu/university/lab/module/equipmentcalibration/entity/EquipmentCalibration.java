package edu.university.lab.module.equipmentcalibration.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.university.lab.common.entity.BaseEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备校准实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("equipment_calibration")
public class EquipmentCalibration extends BaseEntity {

    @TableId
    private Integer id;

    private Integer equipmentId;

    private Integer laboratoryId;

    private Integer calibrationUserId;

    private String certificateNo;

    private LocalDateTime calibrationDate;

    private LocalDateTime validUntil;

    private Integer calibrationResult;

    private Integer calibrationStatus;

    private String remarks;
}
