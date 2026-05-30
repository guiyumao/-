package edu.university.lab.module.equipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.common.service.BaseCrudService;
import edu.university.lab.module.equipment.entity.Equipment;
import edu.university.lab.module.equipment.mapper.EquipmentMapper;
import edu.university.lab.module.equipment.service.EquipmentService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EquipmentServiceImpl extends BaseCrudService<EquipmentMapper, Equipment> implements EquipmentService {

    @Override
    public Page<Equipment> pageQuery(PageQuery query) {
        LambdaQueryWrapper<Equipment> wrapper = new LambdaQueryWrapper<Equipment>()
            .orderByDesc(Equipment::getId);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Equipment::getEquipmentName, query.getKeyword())
                .or()
                .like(Equipment::getEquipmentCode, query.getKeyword())
                .or()
                .like(Equipment::getModel, query.getKeyword()));
        }
        return pageQuery(query, wrapper);
    }
}
