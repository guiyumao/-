package edu.university.lab.module.reminder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.university.lab.auth.security.LoginUser;
import edu.university.lab.auth.security.SecurityUtils;
import edu.university.lab.common.constant.RoleConstants;
import edu.university.lab.module.consumable.entity.Consumable;
import edu.university.lab.module.consumable.mapper.ConsumableMapper;
import edu.university.lab.module.equipment.entity.Equipment;
import edu.university.lab.module.equipment.mapper.EquipmentMapper;
import edu.university.lab.module.inventory.entity.Inventory;
import edu.university.lab.module.inventory.mapper.InventoryMapper;
import edu.university.lab.module.laboratory.entity.Laboratory;
import edu.university.lab.module.laboratory.mapper.LaboratoryMapper;
import edu.university.lab.module.reminder.dto.ReminderItem;
import edu.university.lab.module.reminder.dto.ReminderResponse;
import edu.university.lab.module.reminder.service.ReminderService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReminderServiceImpl implements ReminderService {

    private final EquipmentMapper equipmentMapper;

    private final LaboratoryMapper laboratoryMapper;

    private final InventoryMapper inventoryMapper;

    private final ConsumableMapper consumableMapper;

    @Value("${app.reminder.days:30}")
    private int reminderDays;

    @Override
    public ReminderResponse listReminders() {
        return ReminderResponse.builder()
            .pendingCalibrations(loadPendingCalibrations())
            .expiringConsumables(loadExpiringConsumables())
            .build();
    }

    @Override
    public long countPendingCalibrations() {
        return loadPendingCalibrations().size();
    }

    @Override
    public long countExpiringConsumables() {
        return loadExpiringConsumables().size();
    }

    private List<ReminderItem> loadPendingCalibrations() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.plusDays(reminderDays);

        LambdaQueryWrapper<Equipment> wrapper = new LambdaQueryWrapper<Equipment>()
            .isNotNull(Equipment::getNextCalibrationDate)
            .between(Equipment::getNextCalibrationDate, now, deadline)
            .orderByAsc(Equipment::getNextCalibrationDate);
        if (limitByLaboratory(loginUser)) {
            wrapper.eq(Equipment::getLaboratoryId, loginUser.getUser().getLaboratoryId());
        }
        return equipmentMapper.selectList(wrapper).stream()
            .map(item -> ReminderItem.builder()
                .reminderType("calibration")
                .itemName(item.getEquipmentName())
                .laboratoryName(findLaboratoryName(item.getLaboratoryId()))
                .dueDate(item.getNextCalibrationDate())
                .remainingQuantity(BigDecimal.ONE)
                .build())
            .toList();
    }

    private List<ReminderItem> loadExpiringConsumables() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.plusDays(reminderDays);

        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<Inventory>()
            .eq(Inventory::getItemType, 2)
            .gt(Inventory::getQuantity, BigDecimal.ZERO)
            .isNotNull(Inventory::getExpiryDate)
            .between(Inventory::getExpiryDate, now, deadline)
            .orderByAsc(Inventory::getExpiryDate);
        if (limitByLaboratory(loginUser)) {
            wrapper.eq(Inventory::getLaboratoryId, loginUser.getUser().getLaboratoryId());
        }
        return inventoryMapper.selectList(wrapper).stream()
            .sorted(Comparator.comparing(Inventory::getExpiryDate))
            .map(item -> ReminderItem.builder()
                .reminderType("consumable")
                .itemName(findConsumableName(item.getItemId()))
                .laboratoryName(findLaboratoryName(item.getLaboratoryId()))
                .batchNo(item.getBatchNo())
                .dueDate(item.getExpiryDate())
                .remainingQuantity(item.getQuantity())
                .build())
            .toList();
    }

    private boolean limitByLaboratory(LoginUser loginUser) {
        return loginUser != null
            && loginUser.getUser() != null
            && loginUser.getUser().getLaboratoryId() != null
            && loginUser.getRoleCodes().stream().noneMatch(RoleConstants.LAB_SCOPE_BYPASS_ROLES::contains);
    }

    private String findLaboratoryName(Integer laboratoryId) {
        Laboratory laboratory = laboratoryId == null ? null : laboratoryMapper.selectById(laboratoryId);
        return laboratory == null ? "" : laboratory.getLaboratoryName();
    }

    private String findConsumableName(Integer consumableId) {
        Consumable consumable = consumableId == null ? null : consumableMapper.selectById(consumableId);
        return consumable == null ? "" : consumable.getConsumableName();
    }
}
