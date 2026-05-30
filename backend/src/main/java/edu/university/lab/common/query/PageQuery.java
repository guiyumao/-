package edu.university.lab.common.query;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 通用分页查询参数
 */
@Data
public class PageQuery {

    @Min(1)
    private long current = 1;

    @Min(1)
    @Max(100)
    private long pageSize = 10;

    private String keyword;
}
