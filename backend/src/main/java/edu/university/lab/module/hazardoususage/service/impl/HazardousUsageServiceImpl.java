package edu.university.lab.module.hazardoususage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.university.lab.common.audit.AuditLog;
import edu.university.lab.common.constant.Messages;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.common.service.BaseCrudService;
import edu.university.lab.module.hazardoususage.entity.HazardousUsage;
import edu.university.lab.module.hazardoususage.mapper.HazardousUsageMapper;
import edu.university.lab.module.hazardoususage.service.HazardousUsageService;
import edu.university.lab.module.inventory.entity.Inventory;
import edu.university.lab.module.inventory.mapper.InventoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HazardousUsageServiceImpl extends BaseCrudService<HazardousUsageMapper, HazardousUsage> implements HazardousUsageService {

    private final InventoryMapper inventoryMapper;

    public HazardousUsageServiceImpl(InventoryMapper inventoryMapper) {
        this.inventoryMapper = inventoryMapper;
    }

    @Override
    public Page<HazardousUsage> pageQuery(PageQuery query) {
        return pageQuery(query, new LambdaQueryWrapper<HazardousUsage>().orderByDesc(HazardousUsage::getId));
    }

    @Override
    @AuditLog("领用危化品")
    @Transactional(rollbackFor = Exception.class)
    public HazardousUsage createUsage(HazardousUsage usage) {
        if (usage.getActionType() == 2 || usage.getActionType() == 4) {
            Inventory inventory = inventoryMapper.selectOne(new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getLaboratoryId, usage.getLaboratoryId())
                .eq(Inventory::getItemType, 3)
                .eq(Inventory::getItemId, usage.getHazardousMaterialId())
                .eq(Inventory::getBatchNo, usage.getBatchNo())
                .last("LIMIT 1"));
            if (inventory == null || inventory.getQuantity().compareTo(usage.getQuantity()) < 0) {
                throw new IllegalStateException(Messages.INSUFFICIENT_HAZARDOUS_INVENTORY);
            }
        }
        usage.setUsageStatus(2);
        save(usage);
        return usage;
    }
}
