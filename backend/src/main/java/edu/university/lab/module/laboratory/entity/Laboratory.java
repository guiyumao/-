package edu.university.lab.module.laboratory.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.university.lab.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 实验室实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("laboratory")
public class Laboratory extends BaseEntity {

    @TableId
    private Integer id;

    private Integer directorUserId;

    private String laboratoryCode;

    private String laboratoryName;

    private String location;

    private String contactPhone;

    private Integer safetyLevel;

    private Integer status;
}
