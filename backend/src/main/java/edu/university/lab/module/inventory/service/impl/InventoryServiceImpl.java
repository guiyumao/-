package edu.university.lab.module.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.university.lab.module.consumable.entity.Consumable;
import edu.university.lab.module.consumable.mapper.ConsumableMapper;
import edu.university.lab.module.inventory.dto.InventoryAlertQuery;
import edu.university.lab.module.inventory.dto.InventoryAlertRow;
import edu.university.lab.module.inventory.dto.InventoryOptionResponse;
import edu.university.lab.module.inventory.entity.Inventory;
import edu.university.lab.module.inventory.mapper.InventoryMapper;
import edu.university.lab.module.inventory.service.InventoryService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory> implements InventoryService {

    private final ConsumableMapper consumableMapper;

    public InventoryServiceImpl(ConsumableMapper consumableMapper) {
        this.consumableMapper = consumableMapper;
    }

    @Override
    public Page<InventoryAlertRow> pageAlerts(InventoryAlertQuery query) {
        List<InventoryAlertRow> rows = consumableMapper.selectList(new LambdaQueryWrapper<Consumable>()
                .orderByAsc(Consumable::getConsumableCode)
                .orderByAsc(Consumable::getId))
            .stream()
            .map(consumable -> {
                BigDecimal currentStock = lambdaQuery()
                    .eq(Inventory::getItemType, 2)
                    .eq(Inventory::getItemId, consumable.getId())
                    .list()
                    .stream()
                    .map(Inventory::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                return InventoryAlertRow.builder()
                    .consumableId(consumable.getId())
                    .consumableCode(consumable.getConsumableCode())
                    .consumableName(consumable.getConsumableName())
                    .currentStock(currentStock)
                    .safetyStock(consumable.getSafetyStock() == null ? BigDecimal.ZERO : consumable.getSafetyStock())
                    .storageLocation(consumable.getStorageLocation())
                    .build();
            })
            .filter(item -> item.getCurrentStock().compareTo(item.getSafetyStock()) < 0)
            .filter(item -> !StringUtils.hasText(query.getKeyword())
                || item.getConsumableName().contains(query.getKeyword())
                || item.getConsumableCode().contains(query.getKeyword()))
            .toList();

        long start = (query.getCurrent() - 1) * query.getPageSize();
        long end = Math.min(start + query.getPageSize(), rows.size());
        List<InventoryAlertRow> pageRecords = start >= rows.size() ? List.of() : rows.subList((int) start, (int) end);

        Page<InventoryAlertRow> page = new Page<>(query.getCurrent(), query.getPageSize(), rows.size());
        page.setRecords(pageRecords);
        return page;
    }

    @Override
    public List<InventoryOptionResponse> listAvailableOptions(Integer laboratoryId, Integer itemType, Integer itemId) {
        return list(new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getLaboratoryId, laboratoryId)
                .eq(Inventory::getItemType, itemType)
                .eq(Inventory::getItemId, itemId)
                .gt(Inventory::getQuantity, BigDecimal.ZERO)
                .orderByAsc(Inventory::getExpiryDate)
                .orderByDesc(Inventory::getLastStockInTime))
            .stream()
            .map(item -> InventoryOptionResponse.builder()
                .id(item.getId())
                .laboratoryId(item.getLaboratoryId())
                .itemType(item.getItemType())
                .itemId(item.getItemId())
                .batchNo(item.getBatchNo())
                .quantity(item.getQuantity())
                .lockedQuantity(item.getLockedQuantity())
                .availableQuantity(item.getQuantity().subtract(item.getLockedQuantity() == null ? BigDecimal.ZERO : item.getLockedQuantity()))
                .unitPrice(item.getUnitPrice())
                .productionDate(item.getProductionDate())
                .expiryDate(item.getExpiryDate())
                .warningStatus(item.getWarningStatus())
                .build())
            .toList();
    }
}
