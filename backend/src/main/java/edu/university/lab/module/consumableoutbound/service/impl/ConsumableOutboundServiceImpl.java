package edu.university.lab.module.consumableoutbound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.university.lab.common.audit.AuditLog;
import edu.university.lab.common.constant.Messages;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.common.service.BaseCrudService;
import edu.university.lab.module.consumableoutbound.entity.ConsumableOutbound;
import edu.university.lab.module.consumableoutbound.mapper.ConsumableOutboundMapper;
import edu.university.lab.module.consumableoutbound.service.ConsumableOutboundService;
import edu.university.lab.module.inventory.entity.Inventory;
import edu.university.lab.module.inventory.mapper.InventoryMapper;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsumableOutboundServiceImpl extends BaseCrudService<ConsumableOutboundMapper, ConsumableOutbound> implements ConsumableOutboundService {

    private final InventoryMapper inventoryMapper;

    public ConsumableOutboundServiceImpl(InventoryMapper inventoryMapper) {
        this.inventoryMapper = inventoryMapper;
    }

    @Override
    public Page<ConsumableOutbound> pageQuery(PageQuery query) {
        return pageQuery(query, new LambdaQueryWrapper<ConsumableOutbound>().orderByDesc(ConsumableOutbound::getId));
    }

    @Override
    @AuditLog("出库耗材")
    @Transactional(rollbackFor = Exception.class)
    public ConsumableOutbound createOutbound(ConsumableOutbound outbound) {
        Inventory inventory = inventoryMapper.selectOne(new LambdaQueryWrapper<Inventory>()
            .eq(Inventory::getLaboratoryId, outbound.getLaboratoryId())
            .eq(Inventory::getItemType, 2)
            .eq(Inventory::getItemId, outbound.getConsumableId())
            .eq(Inventory::getBatchNo, outbound.getBatchNo())
            .last("LIMIT 1"));
        if (inventory == null || inventory.getQuantity().compareTo(outbound.getQuantity()) < 0) {
            throw new IllegalStateException(Messages.INSUFFICIENT_INVENTORY);
        }

        outbound.setUnitPrice(inventory.getUnitPrice());
        outbound.setTotalAmount(inventory.getUnitPrice().multiply(outbound.getQuantity()));
        outbound.setOutboundStatus(2);
        save(outbound);
        return outbound;
    }
}
