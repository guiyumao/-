package edu.university.lab.module.equipmentborrow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.common.service.BaseCrudService;
import edu.university.lab.module.equipmentborrow.entity.EquipmentBorrow;
import edu.university.lab.module.equipmentborrow.mapper.EquipmentBorrowMapper;
import edu.university.lab.module.equipmentborrow.service.EquipmentBorrowService;
import org.springframework.stereotype.Service;

@Service
public class EquipmentBorrowServiceImpl extends BaseCrudService<EquipmentBorrowMapper, EquipmentBorrow> implements EquipmentBorrowService {

    @Override
    public Page<EquipmentBorrow> pageQuery(PageQuery query) {
        LambdaQueryWrapper<EquipmentBorrow> wrapper = new LambdaQueryWrapper<EquipmentBorrow>()
            .orderByDesc(EquipmentBorrow::getId);
        return pageQuery(query, wrapper);
    }
}
