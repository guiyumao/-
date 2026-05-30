package edu.university.lab.module.equipmentborrow.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.university.lab.common.entity.BaseEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备借用实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("equipment_borrow")
public class EquipmentBorrow extends BaseEntity {

    @TableId
    private Integer id;

    private Integer equipmentId;

    private Integer laboratoryId;

    private Integer borrowerUserId;

    private Integer approverUserId;

    private String purpose;

    private LocalDateTime borrowDate;

    private LocalDateTime dueDate;

    private LocalDateTime actualReturnDate;

    private Integer borrowStatus;

    private String returnCondition;

    private String remarks;
}
