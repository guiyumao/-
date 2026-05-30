package edu.university.lab.module.equipmentcategory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.module.equipmentcategory.entity.EquipmentCategory;

public interface EquipmentCategoryService extends IService<EquipmentCategory> {

    Page<EquipmentCategory> pageQuery(PageQuery query);
}
