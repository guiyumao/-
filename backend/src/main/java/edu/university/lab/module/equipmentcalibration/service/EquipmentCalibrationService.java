package edu.university.lab.module.equipmentcalibration.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.module.equipmentcalibration.entity.EquipmentCalibration;

public interface EquipmentCalibrationService extends IService<EquipmentCalibration> {

    Page<EquipmentCalibration> pageQuery(PageQuery query);

    EquipmentCalibration createCalibration(EquipmentCalibration request);

    EquipmentCalibration confirmCalibration(Integer id, EquipmentCalibration request);
}
