package edu.university.lab.module.consumablecategory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.module.consumablecategory.entity.ConsumableCategory;

public interface ConsumableCategoryService extends IService<ConsumableCategory> {

    Page<ConsumableCategory> pageQuery(PageQuery query);
}
