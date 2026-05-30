package edu.university.lab.module.dashboard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.university.lab.module.consumable.entity.Consumable;
import edu.university.lab.module.consumable.mapper.ConsumableMapper;
import edu.university.lab.module.consumableoutbound.entity.ConsumableOutbound;
import edu.university.lab.module.consumableoutbound.mapper.ConsumableOutboundMapper;
import edu.university.lab.module.dashboard.dto.DashboardStatsResponse;
import edu.university.lab.module.dashboard.dto.SimpleChartItem;
import edu.university.lab.module.dashboard.service.DashboardService;
import edu.university.lab.module.equipment.entity.Equipment;
import edu.university.lab.module.equipment.mapper.EquipmentMapper;
import edu.university.lab.module.equipmentborrow.entity.EquipmentBorrow;
import edu.university.lab.module.equipmentborrow.mapper.EquipmentBorrowMapper;
import edu.university.lab.module.hazardousmaterial.entity.HazardousMaterial;
import edu.university.lab.module.hazardousmaterial.mapper.HazardousMaterialMapper;
import edu.university.lab.module.laboratory.entity.Laboratory;
import edu.university.lab.module.laboratory.mapper.LaboratoryMapper;
import edu.university.lab.module.reminder.service.ReminderService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final LaboratoryMapper laboratoryMapper;
    private final EquipmentMapper equipmentMapper;
    private final ConsumableMapper consumableMapper;
    private final HazardousMaterialMapper hazardousMaterialMapper;
    private final EquipmentBorrowMapper equipmentBorrowMapper;
    private final ConsumableOutboundMapper consumableOutboundMapper;
    private final ReminderService reminderService;

    @Override
    public DashboardStatsResponse stats() {
        LocalDateTime now = LocalDateTime.now();
        List<SimpleChartItem> trend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = 5; i >= 0; i--) {
            LocalDateTime start = now.minusDays(i).withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime end = start.plusDays(1);
            BigDecimal amount = consumableOutboundMapper.selectList(new LambdaQueryWrapper<ConsumableOutbound>()
                    .ge(ConsumableOutbound::getOutboundDate, start)
                    .lt(ConsumableOutbound::getOutboundDate, end))
                .stream()
                .map(ConsumableOutbound::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            trend.add(new SimpleChartItem(start.format(formatter), amount));
        }

        BigDecimal monthlyAmount = consumableOutboundMapper.selectList(new LambdaQueryWrapper<ConsumableOutbound>()
                .ge(ConsumableOutbound::getOutboundDate, now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0)))
            .stream()
            .map(ConsumableOutbound::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardStatsResponse.builder()
            .laboratoryCount(laboratoryMapper.selectCount(new LambdaQueryWrapper<Laboratory>()))
            .equipmentCount(equipmentMapper.selectCount(new LambdaQueryWrapper<Equipment>()))
            .consumableCount(consumableMapper.selectCount(new LambdaQueryWrapper<Consumable>()))
            .hazardousCount(hazardousMaterialMapper.selectCount(new LambdaQueryWrapper<HazardousMaterial>()))
            .currentBorrowCount(equipmentBorrowMapper.selectCount(new LambdaQueryWrapper<EquipmentBorrow>()
                .in(EquipmentBorrow::getBorrowStatus, 2, 4)))
            .monthlyConsumableAmount(monthlyAmount)
            .pendingCalibrations(reminderService.countPendingCalibrations())
            .expiringConsumables(reminderService.countExpiringConsumables())
            .consumableTrend(trend)
            .build();
    }
}
