package edu.university.lab.module.equipmentcategory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.common.service.BaseCrudService;
import edu.university.lab.module.equipmentcategory.entity.EquipmentCategory;
import edu.university.lab.module.equipmentcategory.mapper.EquipmentCategoryMapper;
import edu.university.lab.module.equipmentcategory.service.EquipmentCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EquipmentCategoryServiceImpl extends BaseCrudService<EquipmentCategoryMapper, EquipmentCategory> implements EquipmentCategoryService {

    @Override
    public Page<EquipmentCategory> pageQuery(PageQuery query) {
        LambdaQueryWrapper<EquipmentCategory> wrapper = new LambdaQueryWrapper<EquipmentCategory>()
            .orderByDesc(EquipmentCategory::getId);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(EquipmentCategory::getCategoryName, query.getKeyword())
                .or()
                .like(EquipmentCategory::getCategoryCode, query.getKeyword()));
        }
        return pageQuery(query, wrapper);
    }
}
