package edu.university.lab.module.inventory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import edu.university.lab.module.inventory.dto.InventoryAlertQuery;
import edu.university.lab.module.inventory.dto.InventoryAlertRow;
import edu.university.lab.module.inventory.dto.InventoryOptionResponse;
import edu.university.lab.module.inventory.entity.Inventory;
import java.util.List;

public interface InventoryService extends IService<Inventory> {

    Page<InventoryAlertRow> pageAlerts(InventoryAlertQuery query);

    List<InventoryOptionResponse> listAvailableOptions(Integer laboratoryId, Integer itemType, Integer itemId);
}
