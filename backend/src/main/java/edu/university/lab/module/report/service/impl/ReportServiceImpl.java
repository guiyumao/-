package edu.university.lab.module.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.university.lab.module.consumable.entity.Consumable;
import edu.university.lab.module.consumable.mapper.ConsumableMapper;
import edu.university.lab.module.consumableoutbound.entity.ConsumableOutbound;
import edu.university.lab.module.consumableoutbound.mapper.ConsumableOutboundMapper;
import edu.university.lab.module.equipment.entity.Equipment;
import edu.university.lab.module.equipment.mapper.EquipmentMapper;
import edu.university.lab.module.equipmentborrow.entity.EquipmentBorrow;
import edu.university.lab.module.equipmentborrow.mapper.EquipmentBorrowMapper;
import edu.university.lab.module.equipmentcalibration.entity.EquipmentCalibration;
import edu.university.lab.module.equipmentcalibration.mapper.EquipmentCalibrationMapper;
import edu.university.lab.module.equipmentrepair.entity.EquipmentRepair;
import edu.university.lab.module.equipmentrepair.mapper.EquipmentRepairMapper;
import edu.university.lab.module.hazardousmaterial.entity.HazardousMaterial;
import edu.university.lab.module.hazardousmaterial.mapper.HazardousMaterialMapper;
import edu.university.lab.module.hazardoususage.entity.HazardousUsage;
import edu.university.lab.module.hazardoususage.mapper.HazardousUsageMapper;
import edu.university.lab.module.laboratory.entity.Laboratory;
import edu.university.lab.module.laboratory.mapper.LaboratoryMapper;
import edu.university.lab.module.report.dto.ReportQuery;
import edu.university.lab.module.report.dto.ReportRow;
import edu.university.lab.module.report.service.ReportService;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final LaboratoryMapper laboratoryMapper;
    private final EquipmentMapper equipmentMapper;
    private final EquipmentBorrowMapper equipmentBorrowMapper;
    private final EquipmentRepairMapper equipmentRepairMapper;
    private final EquipmentCalibrationMapper equipmentCalibrationMapper;
    private final ConsumableMapper consumableMapper;
    private final ConsumableOutboundMapper consumableOutboundMapper;
    private final HazardousMaterialMapper hazardousMaterialMapper;
    private final HazardousUsageMapper hazardousUsageMapper;

    @Override
    public Page<ReportRow> summaryRows(ReportQuery query) {
        List<ReportRow> rows = new ArrayList<>();

        equipmentBorrowMapper.selectList(buildBorrowWrapper(query))
            .forEach(item -> rows.add(ReportRow.builder()
                .reportType("equipment_borrow")
                .itemCode(findEquipmentCode(item.getEquipmentId()))
                .itemName(findEquipmentName(item.getEquipmentId()))
                .laboratoryName(findLaboratoryName(item.getLaboratoryId()))
                .laboratoryId(item.getLaboratoryId())
                .businessDate(formatDate(item.getBorrowDate()))
                .quantity("1")
                .amount("-")
                .statusText(String.valueOf(item.getBorrowStatus()))
                .build()));

        equipmentRepairMapper.selectList(buildRepairWrapper(query))
            .forEach(item -> rows.add(ReportRow.builder()
                .reportType("equipment_repair")
                .itemCode(findEquipmentCode(item.getEquipmentId()))
                .itemName(findEquipmentName(item.getEquipmentId()))
                .laboratoryName(findLaboratoryName(item.getLaboratoryId()))
                .laboratoryId(item.getLaboratoryId())
                .businessDate(formatDate(item.getReportTime()))
                .quantity("1")
                .amount(item.getRepairCost() == null ? "0.00" : item.getRepairCost().toPlainString())
                .statusText(String.valueOf(item.getRepairStatus()))
                .build()));

        equipmentCalibrationMapper.selectList(buildCalibrationWrapper(query))
            .forEach(item -> rows.add(ReportRow.builder()
                .reportType("equipment_calibration")
                .itemCode(findEquipmentCode(item.getEquipmentId()))
                .itemName(findEquipmentName(item.getEquipmentId()))
                .laboratoryName(findLaboratoryName(item.getLaboratoryId()))
                .laboratoryId(item.getLaboratoryId())
                .businessDate(formatDate(item.getCalibrationDate()))
                .quantity("1")
                .amount("-")
                .statusText(String.valueOf(item.getCalibrationStatus()))
                .build()));

        consumableOutboundMapper.selectList(buildConsumableWrapper(query))
            .forEach(item -> rows.add(ReportRow.builder()
                .reportType("consumable_outbound")
                .itemCode(findConsumableCode(item.getConsumableId()))
                .itemName(findConsumableName(item.getConsumableId()))
                .laboratoryName(findLaboratoryName(item.getLaboratoryId()))
                .laboratoryId(item.getLaboratoryId())
                .businessDate(formatDate(item.getOutboundDate()))
                .quantity(item.getQuantity() == null ? "0.00" : item.getQuantity().toPlainString())
                .amount(item.getTotalAmount() == null ? "0.00" : item.getTotalAmount().toPlainString())
                .statusText(String.valueOf(item.getOutboundStatus()))
                .build()));

        hazardousUsageMapper.selectList(buildHazardousWrapper(query))
            .forEach(item -> rows.add(ReportRow.builder()
                .reportType("hazardous_usage")
                .itemCode(findHazardousCode(item.getHazardousMaterialId()))
                .itemName(findHazardousName(item.getHazardousMaterialId()))
                .laboratoryName(findLaboratoryName(item.getLaboratoryId()))
                .laboratoryId(item.getLaboratoryId())
                .businessDate(formatDate(item.getUsageDate()))
                .quantity(item.getQuantity() == null ? "0.00" : item.getQuantity().toPlainString())
                .amount("-")
                .statusText(String.valueOf(item.getUsageStatus()))
                .build()));

        List<ReportRow> filtered = rows.stream()
            .filter(row -> matchType(query.getType(), row.getReportType()))
            .sorted((left, right) -> right.getBusinessDate().compareTo(left.getBusinessDate()))
            .toList();

        long start = (query.getCurrent() - 1) * query.getPageSize();
        long end = Math.min(start + query.getPageSize(), filtered.size());
        List<ReportRow> pageRecords = start >= filtered.size() ? List.of() : filtered.subList((int) start, (int) end);

        Page<ReportRow> page = new Page<>(query.getCurrent(), query.getPageSize(), filtered.size());
        page.setRecords(pageRecords);
        return page;
    }

    @Override
    public byte[] exportCsv(ReportQuery query) {
        StringBuilder builder = new StringBuilder();
        builder.append("report_type,item_code,item_name,laboratory_name,business_date,quantity,amount,status_text\n");
        summaryRows(query).getRecords().forEach(row -> builder
            .append(escape(row.getReportType())).append(',')
            .append(escape(row.getItemCode())).append(',')
            .append(escape(row.getItemName())).append(',')
            .append(escape(row.getLaboratoryName())).append(',')
            .append(escape(row.getBusinessDate())).append(',')
            .append(escape(row.getQuantity())).append(',')
            .append(escape(row.getAmount())).append(',')
            .append(escape(row.getStatusText())).append('\n'));
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private LambdaQueryWrapper<EquipmentBorrow> buildBorrowWrapper(ReportQuery query) {
        LambdaQueryWrapper<EquipmentBorrow> wrapper = new LambdaQueryWrapper<EquipmentBorrow>()
            .orderByDesc(EquipmentBorrow::getBorrowDate)
            .orderByDesc(EquipmentBorrow::getId);
        applyDateRange(wrapper, EquipmentBorrow::getBorrowDate, query.getStartDate(), query.getEndDate());
        if (query.getLabId() != null) {
            wrapper.eq(EquipmentBorrow::getLaboratoryId, query.getLabId());
        }
        return wrapper.last("LIMIT 50");
    }

    private LambdaQueryWrapper<EquipmentRepair> buildRepairWrapper(ReportQuery query) {
        LambdaQueryWrapper<EquipmentRepair> wrapper = new LambdaQueryWrapper<EquipmentRepair>()
            .orderByDesc(EquipmentRepair::getReportTime)
            .orderByDesc(EquipmentRepair::getId);
        applyDateRange(wrapper, EquipmentRepair::getReportTime, query.getStartDate(), query.getEndDate());
        if (query.getLabId() != null) {
            wrapper.eq(EquipmentRepair::getLaboratoryId, query.getLabId());
        }
        return wrapper.last("LIMIT 50");
    }

    private LambdaQueryWrapper<EquipmentCalibration> buildCalibrationWrapper(ReportQuery query) {
        LambdaQueryWrapper<EquipmentCalibration> wrapper = new LambdaQueryWrapper<EquipmentCalibration>()
            .orderByDesc(EquipmentCalibration::getCalibrationDate)
            .orderByDesc(EquipmentCalibration::getId);
        applyDateRange(wrapper, EquipmentCalibration::getCalibrationDate, query.getStartDate(), query.getEndDate());
        if (query.getLabId() != null) {
            wrapper.eq(EquipmentCalibration::getLaboratoryId, query.getLabId());
        }
        return wrapper.last("LIMIT 50");
    }

    private LambdaQueryWrapper<ConsumableOutbound> buildConsumableWrapper(ReportQuery query) {
        LambdaQueryWrapper<ConsumableOutbound> wrapper = new LambdaQueryWrapper<ConsumableOutbound>()
            .orderByDesc(ConsumableOutbound::getOutboundDate)
            .orderByDesc(ConsumableOutbound::getId);
        applyDateRange(wrapper, ConsumableOutbound::getOutboundDate, query.getStartDate(), query.getEndDate());
        if (query.getLabId() != null) {
            wrapper.eq(ConsumableOutbound::getLaboratoryId, query.getLabId());
        }
        return wrapper.last("LIMIT 50");
    }

    private LambdaQueryWrapper<HazardousUsage> buildHazardousWrapper(ReportQuery query) {
        LambdaQueryWrapper<HazardousUsage> wrapper = new LambdaQueryWrapper<HazardousUsage>()
            .orderByDesc(HazardousUsage::getUsageDate)
            .orderByDesc(HazardousUsage::getId);
        applyDateRange(wrapper, HazardousUsage::getUsageDate, query.getStartDate(), query.getEndDate());
        if (query.getLabId() != null) {
            wrapper.eq(HazardousUsage::getLaboratoryId, query.getLabId());
        }
        return wrapper.last("LIMIT 50");
    }

    private <T> void applyDateRange(
        LambdaQueryWrapper<T> wrapper,
        SFunction<T, LocalDateTime> column,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        if (startDate != null) {
            wrapper.ge(column, startDate);
        }
        if (endDate != null) {
            wrapper.le(column, endDate);
        }
    }

    private boolean matchType(String type, String reportType) {
        if (!StringUtils.hasText(type)) {
            return true;
        }
        return switch (type) {
            case "borrow" -> "equipment_borrow".equals(reportType);
            case "repair" -> "equipment_repair".equals(reportType);
            case "calibration" -> "equipment_calibration".equals(reportType);
            case "consumable" -> "consumable_outbound".equals(reportType);
            case "hazardous" -> "hazardous_usage".equals(reportType);
            default -> true;
        };
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(FORMATTER);
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private String findLaboratoryName(Integer id) {
        Laboratory entity = id == null ? null : laboratoryMapper.selectById(id);
        return entity == null ? "" : entity.getLaboratoryName();
    }

    private String findEquipmentCode(Integer id) {
        Equipment entity = id == null ? null : equipmentMapper.selectById(id);
        return entity == null ? "" : entity.getEquipmentCode();
    }

    private String findEquipmentName(Integer id) {
        Equipment entity = id == null ? null : equipmentMapper.selectById(id);
        return entity == null ? "" : entity.getEquipmentName();
    }

    private String findConsumableCode(Integer id) {
        Consumable entity = id == null ? null : consumableMapper.selectById(id);
        return entity == null ? "" : entity.getConsumableCode();
    }

    private String findConsumableName(Integer id) {
        Consumable entity = id == null ? null : consumableMapper.selectById(id);
        return entity == null ? "" : entity.getConsumableName();
    }

    private String findHazardousCode(Integer id) {
        HazardousMaterial entity = id == null ? null : hazardousMaterialMapper.selectById(id);
        return entity == null ? "" : entity.getHazardousCode();
    }

    private String findHazardousName(Integer id) {
        HazardousMaterial entity = id == null ? null : hazardousMaterialMapper.selectById(id);
        return entity == null ? "" : entity.getMaterialName();
    }
}
