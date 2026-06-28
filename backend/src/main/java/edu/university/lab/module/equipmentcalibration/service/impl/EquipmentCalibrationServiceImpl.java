package edu.university.lab.module.equipmentcalibration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.university.lab.common.audit.AuditLog;
import edu.university.lab.common.constant.Messages;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.common.service.BaseCrudService;
import edu.university.lab.module.equipment.entity.Equipment;
import edu.university.lab.module.equipment.service.EquipmentService;
import edu.university.lab.module.equipmentcalibration.entity.EquipmentCalibration;
import edu.university.lab.module.equipmentcalibration.mapper.EquipmentCalibrationMapper;
import edu.university.lab.module.equipmentcalibration.service.EquipmentCalibrationService;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class EquipmentCalibrationServiceImpl extends BaseCrudService<EquipmentCalibrationMapper, EquipmentCalibration> implements EquipmentCalibrationService {

    private final EquipmentService equipmentService;

    @Override
    public Page<EquipmentCalibration> pageQuery(PageQuery query) {
        LambdaQueryWrapper<EquipmentCalibration> wrapper = new LambdaQueryWrapper<EquipmentCalibration>()
            .orderByDesc(EquipmentCalibration::getId);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(EquipmentCalibration::getCertificateNo, query.getKeyword())
                .or()
                .like(EquipmentCalibration::getRemarks, query.getKeyword()));
        }
        return pageQuery(query, wrapper);
    }

    @Override
    @AuditLog("创建校准任务")
    @Transactional(rollbackFor = Exception.class)
    public EquipmentCalibration createCalibration(EquipmentCalibration request) {
        Equipment equipment = equipmentService.getById(request.getEquipmentId());
        if (equipment == null) {
            throw new IllegalArgumentException(Messages.EQUIPMENT_NOT_FOUND);
        }
        if (request.getCalibrationDate() == null) {
            request.setCalibrationDate(LocalDateTime.now());
        }
        if (request.getValidUntil() == null || request.getValidUntil().isBefore(request.getCalibrationDate())) {
            throw new IllegalArgumentException(Messages.CALIBRATION_DATE_INVALID);
        }
        if (request.getCalibrationStatus() == null) {
            request.setCalibrationStatus(1);
        }
        save(request);
        equipment.setStatus(4);
        equipmentService.updateById(equipment);
        return request;
    }

    @Override
    @AuditLog("确认校准结果")
    @Transactional(rollbackFor = Exception.class)
    public EquipmentCalibration confirmCalibration(Integer id, EquipmentCalibration request) {
        EquipmentCalibration calibration = getById(id);
        if (calibration == null) {
            throw new IllegalArgumentException(Messages.CALIBRATION_NOT_FOUND);
        }
        calibration.setCalibrationUserId(request.getCalibrationUserId());
        calibration.setCalibrationDate(request.getCalibrationDate() == null ? calibration.getCalibrationDate() : request.getCalibrationDate());
        calibration.setValidUntil(request.getValidUntil() == null ? calibration.getValidUntil() : request.getValidUntil());
        calibration.setCalibrationResult(request.getCalibrationResult());
        calibration.setCalibrationStatus(request.getCalibrationStatus() == null ? 2 : request.getCalibrationStatus());
        calibration.setRemarks(request.getRemarks());
        updateById(calibration);

        Equipment equipment = equipmentService.getById(calibration.getEquipmentId());
        if (equipment != null) {
            equipment.setLastCalibrationDate(calibration.getCalibrationDate());
            equipment.setNextCalibrationDate(calibration.getCalibrationDate().plus(
                equipment.getCalibrationCycleDays() == null ? 365L : equipment.getCalibrationCycleDays(),
                ChronoUnit.DAYS
            ));
            equipment.setStatus(calibration.getCalibrationResult() != null && calibration.getCalibrationResult() == 1 ? 1 : 0);
            equipmentService.updateById(equipment);
        }
        return calibration;
    }
}
