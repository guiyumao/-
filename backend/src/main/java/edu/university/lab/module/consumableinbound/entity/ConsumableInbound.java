package edu.university.lab.module.consumableinbound.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.university.lab.common.entity.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 耗材入库实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("consumable_inbound")
public class ConsumableInbound extends BaseEntity {

    @TableId
    private Integer id;

    private Integer consumableId;

    private Integer laboratoryId;

    private Integer operatorUserId;

    private String batchNo;

    private Integer inboundType;

    private BigDecimal quantity;

    private BigDecimal unitPrice;

    private BigDecimal totalAmount;

    private LocalDateTime productionDate;

    private LocalDateTime expiryDate;

    private String supplierName;

    private LocalDateTime inboundDate;

    private String remarks;
}
