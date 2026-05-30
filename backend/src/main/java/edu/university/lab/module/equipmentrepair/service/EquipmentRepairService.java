package edu.university.lab.module.equipmentrepair.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.module.equipmentrepair.entity.EquipmentRepair;

public interface EquipmentRepairService extends IService<EquipmentRepair> {

    Page<EquipmentRepair> pageQuery(PageQuery query);

    EquipmentRepair createRepair(EquipmentRepair request);

    EquipmentRepair updateStatus(Integer id, EquipmentRepair request);
}
