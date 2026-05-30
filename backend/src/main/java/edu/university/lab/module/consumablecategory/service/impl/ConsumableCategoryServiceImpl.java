package edu.university.lab.module.consumablecategory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.common.service.BaseCrudService;
import edu.university.lab.module.consumablecategory.entity.ConsumableCategory;
import edu.university.lab.module.consumablecategory.mapper.ConsumableCategoryMapper;
import edu.university.lab.module.consumablecategory.service.ConsumableCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ConsumableCategoryServiceImpl extends BaseCrudService<ConsumableCategoryMapper, ConsumableCategory> implements ConsumableCategoryService {

    @Override
    public Page<ConsumableCategory> pageQuery(PageQuery query) {
        LambdaQueryWrapper<ConsumableCategory> wrapper = new LambdaQueryWrapper<ConsumableCategory>()
            .orderByDesc(ConsumableCategory::getId);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(ConsumableCategory::getCategoryName, query.getKeyword())
                .or()
                .like(ConsumableCategory::getCategoryCode, query.getKeyword()));
        }
        return pageQuery(query, wrapper);
    }
}
