package edu.university.lab.module.equipmentrepair.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.university.lab.common.audit.AuditLog;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.common.service.BaseCrudService;
import edu.university.lab.module.equipment.entity.Equipment;
import edu.university.lab.module.equipment.service.EquipmentService;
import edu.university.lab.module.equipmentrepair.entity.EquipmentRepair;
import edu.university.lab.module.equipmentrepair.mapper.EquipmentRepairMapper;
import edu.university.lab.module.equipmentrepair.service.EquipmentRepairService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class EquipmentRepairServiceImpl extends BaseCrudService<EquipmentRepairMapper, EquipmentRepair> implements EquipmentRepairService {

    private final EquipmentService equipmentService;

    @Override
    public Page<EquipmentRepair> pageQuery(PageQuery query) {
        LambdaQueryWrapper<EquipmentRepair> wrapper = new LambdaQueryWrapper<EquipmentRepair>()
            .orderByDesc(EquipmentRepair::getId);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(EquipmentRepair::getFaultDescription, query.getKeyword())
                .or()
                .like(EquipmentRepair::getRepairResult, query.getKeyword()));
        }
        return pageQuery(query, wrapper);
    }

    @Override
    @AuditLog("提交设备报修")
    @Transactional(rollbackFor = Exception.class)
    public EquipmentRepair createRepair(EquipmentRepair request) {
        Equipment equipment = equipmentService.getById(request.getEquipmentId());
        if (equipment == null) {
            throw new IllegalArgumentException("Equipment not found");
        }
        if (request.getReportTime() == null) {
            request.setReportTime(LocalDateTime.now());
        }
        if (request.getRepairStatus() == null) {
            request.setRepairStatus(1);
        }
        if (request.getRepairCost() == null) {
            request.setRepairCost(BigDecimal.ZERO);
        }
        save(request);
        equipment.setStatus(3);
        equipmentService.updateById(equipment);
        return request;
    }

    @Override
    @AuditLog("更新维修状态")
    @Transactional(rollbackFor = Exception.class)
    public EquipmentRepair updateStatus(Integer id, EquipmentRepair request) {
        EquipmentRepair repair = getById(id);
        if (repair == null) {
            throw new IllegalArgumentException("Repair record not found");
        }
        repair.setRepairUserId(request.getRepairUserId());
        repair.setRepairStatus(request.getRepairStatus());
        repair.setRepairStartTime(request.getRepairStartTime());
        repair.setRepairEndTime(request.getRepairEndTime());
        repair.setRepairCost(request.getRepairCost() == null ? BigDecimal.ZERO : request.getRepairCost());
        repair.setRepairResult(request.getRepairResult());
        repair.setRemarks(request.getRemarks());
        updateById(repair);

        Equipment equipment = equipmentService.getById(repair.getEquipmentId());
        if (equipment != null) {
            if (repair.getRepairStatus() != null && repair.getRepairStatus() == 3) {
                equipment.setStatus(1);
            } else if (repair.getRepairStatus() != null && repair.getRepairStatus() == 4) {
                equipment.setStatus(5);
            } else {
                equipment.setStatus(3);
            }
            equipmentService.updateById(equipment);
        }
        return repair;
    }
}
