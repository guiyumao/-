package edu.university.lab.module.inventory.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.university.lab.common.entity.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库存实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inventory")
public class Inventory extends BaseEntity {

    @TableId
    private Integer id;

    private Integer laboratoryId;

    private Integer itemType;

    private Integer itemId;

    private String batchNo;

    private BigDecimal quantity;

    private BigDecimal lockedQuantity;

    private BigDecimal unitPrice;

    private LocalDateTime productionDate;

    private LocalDateTime expiryDate;

    private LocalDateTime lastStockInTime;

    private LocalDateTime lastStockOutTime;

    private Integer warningStatus;

    private String remarks;
}
