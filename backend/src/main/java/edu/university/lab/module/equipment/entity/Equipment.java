package edu.university.lab.module.equipment.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.university.lab.common.entity.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("equipment")
public class Equipment extends BaseEntity {

    @TableId
    private Integer id;

    private Integer laboratoryId;

    private Integer categoryId;

    private Integer managerUserId;

    private String equipmentCode;

    private String equipmentName;

    private String model;

    private String brand;

    private String manufacturer;

    private LocalDateTime purchaseDate;

    private BigDecimal purchasePrice;

    private Integer serviceLifeYears;

    private String storageLocation;

    private Integer status;

    private Integer calibrationCycleDays;

    private LocalDateTime lastCalibrationDate;

    private LocalDateTime nextCalibrationDate;

    private String remarks;
}
