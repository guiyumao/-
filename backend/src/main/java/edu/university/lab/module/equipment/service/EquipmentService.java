package edu.university.lab.module.equipment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.module.equipment.entity.Equipment;

public interface EquipmentService extends IService<Equipment> {

    Page<Equipment> pageQuery(PageQuery query);
}
