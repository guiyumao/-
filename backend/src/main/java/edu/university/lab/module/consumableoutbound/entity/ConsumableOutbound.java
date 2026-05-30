package edu.university.lab.module.consumableoutbound.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.university.lab.common.entity.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 耗材出库实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("consumable_outbound")
public class ConsumableOutbound extends BaseEntity {

    @TableId
    private Integer id;

    private Integer consumableId;

    private Integer laboratoryId;

    private Integer applicantUserId;

    private Integer approverUserId;

    private Integer operatorUserId;

    private String batchNo;

    private Integer outboundType;

    private BigDecimal quantity;

    private BigDecimal unitPrice;

    private BigDecimal totalAmount;

    private LocalDateTime outboundDate;

    private String purpose;

    private Integer outboundStatus;

    private String remarks;
}
