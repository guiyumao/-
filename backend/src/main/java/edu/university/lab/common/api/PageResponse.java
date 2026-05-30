package edu.university.lab.common.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一分页返回体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private Long current;

    private Long pageSize;

    private Long total;

    private Long pages;

    private List<T> records;

    public static <T> PageResponse<T> from(IPage<T> page) {
        return PageResponse.<T>builder()
            .current(page.getCurrent())
            .pageSize(page.getSize())
            .total(page.getTotal())
            .pages(page.getPages())
            .records(page.getRecords())
            .build();
    }
}
