package edu.university.lab.common.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.university.lab.module.consumable.entity.Consumable;
import edu.university.lab.module.consumable.mapper.ConsumableMapper;
import edu.university.lab.module.consumablecategory.entity.ConsumableCategory;
import edu.university.lab.module.consumablecategory.mapper.ConsumableCategoryMapper;
import edu.university.lab.module.consumableinbound.entity.ConsumableInbound;
import edu.university.lab.module.consumableinbound.mapper.ConsumableInboundMapper;
import edu.university.lab.module.consumableoutbound.entity.ConsumableOutbound;
import edu.university.lab.module.consumableoutbound.mapper.ConsumableOutboundMapper;
import edu.university.lab.module.equipment.entity.Equipment;
import edu.university.lab.module.equipment.mapper.EquipmentMapper;
import edu.university.lab.module.equipmentborrow.entity.EquipmentBorrow;
import edu.university.lab.module.equipmentborrow.mapper.EquipmentBorrowMapper;
import edu.university.lab.module.equipmentcalibration.entity.EquipmentCalibration;
import edu.university.lab.module.equipmentcalibration.mapper.EquipmentCalibrationMapper;
import edu.university.lab.module.equipmentcategory.entity.EquipmentCategory;
import edu.university.lab.module.equipmentcategory.mapper.EquipmentCategoryMapper;
import edu.university.lab.module.equipmentrepair.entity.EquipmentRepair;
import edu.university.lab.module.equipmentrepair.mapper.EquipmentRepairMapper;
import edu.university.lab.module.hazardousmaterial.entity.HazardousMaterial;
import edu.university.lab.module.hazardousmaterial.mapper.HazardousMaterialMapper;
import edu.university.lab.module.hazardoususage.entity.HazardousUsage;
import edu.university.lab.module.hazardoususage.mapper.HazardousUsageMapper;
import edu.university.lab.module.inventory.entity.Inventory;
import edu.university.lab.module.inventory.mapper.InventoryMapper;
import edu.university.lab.module.laboratory.entity.Laboratory;
import edu.university.lab.module.laboratory.mapper.LaboratoryMapper;
import edu.university.lab.module.role.entity.Role;
import edu.university.lab.module.role.mapper.RoleMapper;
import edu.university.lab.module.user.entity.User;
import edu.university.lab.module.user.mapper.UserMapper;
import edu.university.lab.module.userrole.entity.UserRole;
import edu.university.lab.module.userrole.mapper.UserRoleMapper;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(30)
@RequiredArgsConstructor
public class SampleDataInitializer implements ApplicationRunner {

    private final SampleDataProperties properties;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    private final LaboratoryMapper laboratoryMapper;
    private final EquipmentCategoryMapper equipmentCategoryMapper;
    private final ConsumableCategoryMapper consumableCategoryMapper;
    private final EquipmentMapper equipmentMapper;
    private final ConsumableMapper consumableMapper;
    private final HazardousMaterialMapper hazardousMaterialMapper;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final InventoryMapper inventoryMapper;
    private final EquipmentBorrowMapper equipmentBorrowMapper;
    private final EquipmentRepairMapper equipmentRepairMapper;
    private final EquipmentCalibrationMapper equipmentCalibrationMapper;
    private final ConsumableInboundMapper consumableInboundMapper;
    private final ConsumableOutboundMapper consumableOutboundMapper;
    private final HazardousUsageMapper hazardousUsageMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) throws Exception {
        if (!properties.isEnabled()) {
            return;
        }

        DemoDataPayload payload = loadPayload();
        importLaboratories(payload.getLaboratories());
        importEquipmentCategories(payload.getEquipmentCategories());
        importConsumableCategories(payload.getConsumableCategories());
        importEquipment(payload.getEquipment());
        importConsumables(payload.getConsumables());
        importHazardousMaterials(payload.getHazardousMaterials());
        importUsers(payload.getUsers());
        importInventory(payload.getInventory());
        importEquipmentBorrows(payload.getEquipmentBorrows());
        importEquipmentRepairs(payload.getEquipmentRepairs());
        importEquipmentCalibrations(payload.getEquipmentCalibrations());
        importConsumableInbounds(payload.getConsumableInbounds());
        importConsumableOutbounds(payload.getConsumableOutbounds());
        importHazardousUsages(payload.getHazardousUsages());
    }

    private DemoDataPayload loadPayload() throws IOException {
        Resource resource = resourceLoader.getResource(properties.getResource());
        if (!resource.exists()) {
            throw new IllegalStateException("Sample data resource not found: " + properties.getResource());
        }
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, DemoDataPayload.class);
        }
    }

    private void importLaboratories(List<LaboratorySeed> laboratories) {
        for (LaboratorySeed item : safeList(laboratories)) {
            boolean exists = laboratoryMapper.selectCount(new LambdaQueryWrapper<Laboratory>()
                .eq(Laboratory::getLaboratoryCode, item.getLaboratoryCode())) > 0;
            if (exists) {
                continue;
            }
            Laboratory laboratory = new Laboratory();
            laboratory.setLaboratoryCode(item.getLaboratoryCode());
            laboratory.setLaboratoryName(item.getLaboratoryName());
            laboratory.setLocation(item.getLocation());
            laboratory.setContactPhone(item.getContactPhone());
            laboratory.setSafetyLevel(item.getSafetyLevel());
            laboratory.setStatus(item.getStatus());
            laboratoryMapper.insert(laboratory);
        }
    }

    private void importEquipmentCategories(List<CategorySeed> categories) {
        for (CategorySeed item : safeList(categories)) {
            boolean exists = equipmentCategoryMapper.selectCount(new LambdaQueryWrapper<EquipmentCategory>()
                .eq(EquipmentCategory::getCategoryCode, item.getCategoryCode())) > 0;
            if (exists) {
                continue;
            }
            EquipmentCategory category = new EquipmentCategory();
            category.setCategoryCode(item.getCategoryCode());
            category.setCategoryName(item.getCategoryName());
            category.setDescription(item.getDescription());
            category.setStatus(item.getStatus());
            equipmentCategoryMapper.insert(category);
        }
    }

    private void importConsumableCategories(List<CategorySeed> categories) {
        for (CategorySeed item : safeList(categories)) {
            boolean exists = consumableCategoryMapper.selectCount(new LambdaQueryWrapper<ConsumableCategory>()
                .eq(ConsumableCategory::getCategoryCode, item.getCategoryCode())) > 0;
            if (exists) {
                continue;
            }
            ConsumableCategory category = new ConsumableCategory();
            category.setCategoryCode(item.getCategoryCode());
            category.setCategoryName(item.getCategoryName());
            category.setDescription(item.getDescription());
            category.setStatus(item.getStatus());
            consumableCategoryMapper.insert(category);
        }
    }

    private void importEquipment(List<EquipmentSeed> equipmentList) {
        for (EquipmentSeed item : safeList(equipmentList)) {
            boolean exists = equipmentMapper.selectCount(new LambdaQueryWrapper<Equipment>()
                .eq(Equipment::getEquipmentCode, item.getEquipmentCode())) > 0;
            if (exists) {
                continue;
            }

            Laboratory laboratory = requireLaboratory(item.getLaboratoryCode());
            EquipmentCategory category = requireEquipmentCategory(item.getCategoryCode());

            Equipment equipment = new Equipment();
            equipment.setLaboratoryId(laboratory.getId());
            equipment.setCategoryId(category.getId());
            equipment.setEquipmentCode(item.getEquipmentCode());
            equipment.setEquipmentName(item.getEquipmentName());
            equipment.setModel(item.getModel());
            equipment.setBrand(item.getBrand());
            equipment.setManufacturer(item.getManufacturer());
            equipment.setPurchaseDate(item.getPurchaseDate());
            equipment.setPurchasePrice(item.getPurchasePrice());
            equipment.setServiceLifeYears(item.getServiceLifeYears());
            equipment.setStorageLocation(item.getStorageLocation());
            equipment.setStatus(item.getStatus());
            equipment.setCalibrationCycleDays(item.getCalibrationCycleDays());
            equipment.setLastCalibrationDate(item.getLastCalibrationDate());
            equipment.setNextCalibrationDate(item.getNextCalibrationDate());
            equipment.setRemarks(item.getRemarks());
            equipmentMapper.insert(equipment);
        }
    }

    private void importConsumables(List<ConsumableSeed> consumables) {
        for (ConsumableSeed item : safeList(consumables)) {
            boolean exists = consumableMapper.selectCount(new LambdaQueryWrapper<Consumable>()
                .eq(Consumable::getConsumableCode, item.getConsumableCode())) > 0;
            if (exists) {
                continue;
            }

            Laboratory laboratory = requireLaboratory(item.getLaboratoryCode());
            ConsumableCategory category = requireConsumableCategory(item.getCategoryCode());

            Consumable consumable = new Consumable();
            consumable.setLaboratoryId(laboratory.getId());
            consumable.setCategoryId(category.getId());
            consumable.setConsumableCode(item.getConsumableCode());
            consumable.setConsumableName(item.getConsumableName());
            consumable.setSpecification(item.getSpecification());
            consumable.setUnit(item.getUnit());
            consumable.setUnitPrice(item.getUnitPrice());
            consumable.setSafetyStock(item.getSafetyStock());
            consumable.setMaxStock(item.getMaxStock());
            consumable.setStorageLocation(item.getStorageLocation());
            consumable.setExpiryRequired(item.getExpiryRequired());
            consumable.setStatus(item.getStatus());
            consumable.setRemarks(item.getRemarks());
            consumableMapper.insert(consumable);
        }
    }

    private void importHazardousMaterials(List<HazardousMaterialSeed> materials) {
        for (HazardousMaterialSeed item : safeList(materials)) {
            boolean exists = hazardousMaterialMapper.selectCount(new LambdaQueryWrapper<HazardousMaterial>()
                .eq(HazardousMaterial::getHazardousCode, item.getHazardousCode())) > 0;
            if (exists) {
                continue;
            }

            Laboratory laboratory = requireLaboratory(item.getLaboratoryCode());

            HazardousMaterial material = new HazardousMaterial();
            material.setLaboratoryId(laboratory.getId());
            material.setHazardousCode(item.getHazardousCode());
            material.setMaterialName(item.getMaterialName());
            material.setCasNo(item.getCasNo());
            material.setHazardCategory(item.getHazardCategory());
            material.setSpecification(item.getSpecification());
            material.setUnit(item.getUnit());
            material.setConcentration(item.getConcentration());
            material.setStorageLocation(item.getStorageLocation());
            material.setSafetyStock(item.getSafetyStock());
            material.setMsdsCode(item.getMsdsCode());
            material.setStatus(item.getStatus());
            material.setRemarks(item.getRemarks());
            hazardousMaterialMapper.insert(material);
        }
    }

    private void importUsers(List<UserSeed> users) {
        for (UserSeed item : safeList(users)) {
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, item.getUsername())
                .last("LIMIT 1"));
            if (user == null) {
                user = new User();
                user.setLaboratoryId(resolveLaboratoryId(item.getLaboratoryCode()));
                user.setUsername(item.getUsername());
                user.setPasswordHash(passwordEncoder.encode(defaultIfBlank(item.getPassword(), "demo123")));
                user.setRealName(item.getRealName());
                user.setUserNo(item.getUserNo());
                user.setPhone(item.getPhone());
                user.setEmail(item.getEmail());
                user.setGender(item.getGender());
                user.setUserType(item.getUserType());
                user.setStatus(item.getStatus());
                userMapper.insert(user);
            }
            syncUserRoles(user.getId(), item.getRoleCodes());
        }
    }

    private void importInventory(List<InventorySeed> inventoryItems) {
        for (InventorySeed item : safeList(inventoryItems)) {
            Laboratory laboratory = requireLaboratory(item.getLaboratoryCode());
            Integer itemId = resolveInventoryItemId(item);
            boolean exists = inventoryMapper.selectCount(new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getLaboratoryId, laboratory.getId())
                .eq(Inventory::getItemType, item.getItemType())
                .eq(Inventory::getItemId, itemId)
                .eq(Inventory::getBatchNo, item.getBatchNo())) > 0;
            if (exists) {
                continue;
            }
            Inventory inventory = new Inventory();
            inventory.setLaboratoryId(laboratory.getId());
            inventory.setItemType(item.getItemType());
            inventory.setItemId(itemId);
            inventory.setBatchNo(item.getBatchNo());
            inventory.setQuantity(item.getQuantity());
            inventory.setLockedQuantity(item.getLockedQuantity());
            inventory.setUnitPrice(item.getUnitPrice());
            inventory.setProductionDate(item.getProductionDate());
            inventory.setExpiryDate(item.getExpiryDate());
            inventory.setLastStockInTime(item.getLastStockInTime());
            inventory.setLastStockOutTime(item.getLastStockOutTime());
            inventory.setWarningStatus(item.getWarningStatus());
            inventory.setRemarks(item.getRemarks());
            inventoryMapper.insert(inventory);
        }
    }

    private void importEquipmentBorrows(List<EquipmentBorrowSeed> borrows) {
        for (EquipmentBorrowSeed item : safeList(borrows)) {
            Equipment equipment = requireEquipment(item.getEquipmentCode());
            EquipmentBorrow borrow = equipmentBorrowMapper.selectOne(new LambdaQueryWrapper<EquipmentBorrow>()
                .eq(EquipmentBorrow::getEquipmentId, equipment.getId())
                .eq(EquipmentBorrow::getBorrowDate, item.getBorrowDate())
                .last("LIMIT 1"));
            if (borrow == null) {
                borrow = new EquipmentBorrow();
                borrow.setEquipmentId(equipment.getId());
                borrow.setLaboratoryId(equipment.getLaboratoryId());
                borrow.setBorrowDate(item.getBorrowDate());
                borrow.setBorrowerUserId(requireUser(item.getBorrowerUsername()).getId());
                borrow.setApproverUserId(optionalUserId(item.getApproverUsername()));
                borrow.setPurpose(item.getPurpose());
                borrow.setDueDate(item.getDueDate());
                borrow.setActualReturnDate(item.getActualReturnDate());
                borrow.setBorrowStatus(item.getBorrowStatus());
                borrow.setReturnCondition(item.getReturnCondition());
                borrow.setRemarks(item.getRemarks());
                equipmentBorrowMapper.insert(borrow);
                continue;
            }
            borrow.setBorrowerUserId(requireUser(item.getBorrowerUsername()).getId());
            borrow.setApproverUserId(optionalUserId(item.getApproverUsername()));
            borrow.setPurpose(item.getPurpose());
            borrow.setDueDate(item.getDueDate());
            borrow.setActualReturnDate(item.getActualReturnDate());
            borrow.setBorrowStatus(item.getBorrowStatus());
            borrow.setReturnCondition(item.getReturnCondition());
            borrow.setRemarks(item.getRemarks());
            equipmentBorrowMapper.updateById(borrow);
        }
    }

    private void importEquipmentRepairs(List<EquipmentRepairSeed> repairs) {
        for (EquipmentRepairSeed item : safeList(repairs)) {
            Equipment equipment = requireEquipment(item.getEquipmentCode());
            EquipmentRepair repair = equipmentRepairMapper.selectOne(new LambdaQueryWrapper<EquipmentRepair>()
                .eq(EquipmentRepair::getEquipmentId, equipment.getId())
                .eq(EquipmentRepair::getReportTime, item.getReportTime())
                .last("LIMIT 1"));
            if (repair == null) {
                repair = new EquipmentRepair();
                repair.setEquipmentId(equipment.getId());
                repair.setLaboratoryId(equipment.getLaboratoryId());
                repair.setReportTime(item.getReportTime());
                repair.setReporterUserId(requireUser(item.getReporterUsername()).getId());
                repair.setRepairUserId(optionalUserId(item.getRepairUsername()));
                repair.setFaultDescription(item.getFaultDescription());
                repair.setRepairStartTime(item.getRepairStartTime());
                repair.setRepairEndTime(item.getRepairEndTime());
                repair.setRepairStatus(item.getRepairStatus());
                repair.setRepairCost(item.getRepairCost());
                repair.setRepairResult(item.getRepairResult());
                repair.setRemarks(item.getRemarks());
                equipmentRepairMapper.insert(repair);
                continue;
            }
            repair.setReporterUserId(requireUser(item.getReporterUsername()).getId());
            repair.setRepairUserId(optionalUserId(item.getRepairUsername()));
            repair.setFaultDescription(item.getFaultDescription());
            repair.setRepairStartTime(item.getRepairStartTime());
            repair.setRepairEndTime(item.getRepairEndTime());
            repair.setRepairStatus(item.getRepairStatus());
            repair.setRepairCost(item.getRepairCost());
            repair.setRepairResult(item.getRepairResult());
            repair.setRemarks(item.getRemarks());
            equipmentRepairMapper.updateById(repair);
        }
    }

    private void importEquipmentCalibrations(List<EquipmentCalibrationSeed> calibrations) {
        for (EquipmentCalibrationSeed item : safeList(calibrations)) {
            EquipmentCalibration calibration = equipmentCalibrationMapper.selectOne(new LambdaQueryWrapper<EquipmentCalibration>()
                .eq(EquipmentCalibration::getCertificateNo, item.getCertificateNo())
                .last("LIMIT 1"));
            Equipment equipment = requireEquipment(item.getEquipmentCode());
            if (calibration == null) {
                calibration = new EquipmentCalibration();
                calibration.setCertificateNo(item.getCertificateNo());
                calibration.setEquipmentId(equipment.getId());
                calibration.setLaboratoryId(equipment.getLaboratoryId());
                calibration.setCalibrationUserId(optionalUserId(item.getCalibrationUsername()));
                calibration.setCalibrationDate(item.getCalibrationDate());
                calibration.setValidUntil(item.getValidUntil());
                calibration.setCalibrationResult(item.getCalibrationResult());
                calibration.setCalibrationStatus(item.getCalibrationStatus());
                calibration.setRemarks(item.getRemarks());
                equipmentCalibrationMapper.insert(calibration);
                continue;
            }
            calibration.setEquipmentId(equipment.getId());
            calibration.setLaboratoryId(equipment.getLaboratoryId());
            calibration.setCalibrationUserId(optionalUserId(item.getCalibrationUsername()));
            calibration.setCalibrationDate(item.getCalibrationDate());
            calibration.setValidUntil(item.getValidUntil());
            calibration.setCalibrationResult(item.getCalibrationResult());
            calibration.setCalibrationStatus(item.getCalibrationStatus());
            calibration.setRemarks(item.getRemarks());
            equipmentCalibrationMapper.updateById(calibration);
        }
    }

    private void importConsumableInbounds(List<ConsumableInboundSeed> inbounds) {
        for (ConsumableInboundSeed item : safeList(inbounds)) {
            Consumable consumable = requireConsumable(item.getConsumableCode());
            boolean exists = consumableInboundMapper.selectCount(new LambdaQueryWrapper<ConsumableInbound>()
                .eq(ConsumableInbound::getConsumableId, consumable.getId())
                .eq(ConsumableInbound::getBatchNo, item.getBatchNo())
                .eq(ConsumableInbound::getInboundDate, item.getInboundDate())) > 0;
            if (exists) {
                continue;
            }
            ConsumableInbound inbound = new ConsumableInbound();
            inbound.setConsumableId(consumable.getId());
            inbound.setLaboratoryId(consumable.getLaboratoryId());
            inbound.setOperatorUserId(requireUser(item.getOperatorUsername()).getId());
            inbound.setBatchNo(item.getBatchNo());
            inbound.setInboundType(item.getInboundType());
            inbound.setQuantity(item.getQuantity());
            inbound.setUnitPrice(item.getUnitPrice());
            inbound.setTotalAmount(item.getTotalAmount());
            inbound.setProductionDate(item.getProductionDate());
            inbound.setExpiryDate(item.getExpiryDate());
            inbound.setSupplierName(item.getSupplierName());
            inbound.setInboundDate(item.getInboundDate());
            inbound.setRemarks(item.getRemarks());
            consumableInboundMapper.insert(inbound);
        }
    }

    private void importConsumableOutbounds(List<ConsumableOutboundSeed> outbounds) {
        for (ConsumableOutboundSeed item : safeList(outbounds)) {
            Consumable consumable = requireConsumable(item.getConsumableCode());
            ConsumableOutbound outbound = consumableOutboundMapper.selectOne(new LambdaQueryWrapper<ConsumableOutbound>()
                .eq(ConsumableOutbound::getConsumableId, consumable.getId())
                .eq(ConsumableOutbound::getBatchNo, item.getBatchNo())
                .eq(ConsumableOutbound::getOutboundDate, item.getOutboundDate())
                .last("LIMIT 1"));
            if (outbound == null) {
                outbound = new ConsumableOutbound();
                outbound.setConsumableId(consumable.getId());
                outbound.setLaboratoryId(consumable.getLaboratoryId());
                outbound.setApplicantUserId(requireUser(item.getApplicantUsername()).getId());
                outbound.setApproverUserId(optionalUserId(item.getApproverUsername()));
                outbound.setOperatorUserId(optionalUserId(item.getOperatorUsername()));
                outbound.setBatchNo(item.getBatchNo());
                outbound.setOutboundType(item.getOutboundType());
                outbound.setQuantity(item.getQuantity());
                outbound.setUnitPrice(item.getUnitPrice());
                outbound.setTotalAmount(item.getTotalAmount());
                outbound.setOutboundDate(item.getOutboundDate());
                outbound.setPurpose(item.getPurpose());
                outbound.setOutboundStatus(item.getOutboundStatus());
                outbound.setRemarks(item.getRemarks());
                consumableOutboundMapper.insert(outbound);
                continue;
            }
            outbound.setApplicantUserId(requireUser(item.getApplicantUsername()).getId());
            outbound.setApproverUserId(optionalUserId(item.getApproverUsername()));
            outbound.setOperatorUserId(optionalUserId(item.getOperatorUsername()));
            outbound.setOutboundType(item.getOutboundType());
            outbound.setQuantity(item.getQuantity());
            outbound.setUnitPrice(item.getUnitPrice());
            outbound.setTotalAmount(item.getTotalAmount());
            outbound.setPurpose(item.getPurpose());
            outbound.setOutboundStatus(item.getOutboundStatus());
            outbound.setRemarks(item.getRemarks());
            consumableOutboundMapper.updateById(outbound);
        }
    }

    private void importHazardousUsages(List<HazardousUsageSeed> usages) {
        for (HazardousUsageSeed item : safeList(usages)) {
            HazardousMaterial material = requireHazardousMaterial(item.getHazardousCode());
            HazardousUsage usage = hazardousUsageMapper.selectOne(new LambdaQueryWrapper<HazardousUsage>()
                .eq(HazardousUsage::getHazardousMaterialId, material.getId())
                .eq(HazardousUsage::getBatchNo, item.getBatchNo())
                .eq(HazardousUsage::getUsageDate, item.getUsageDate())
                .eq(HazardousUsage::getActionType, item.getActionType())
                .last("LIMIT 1"));
            if (usage == null) {
                usage = new HazardousUsage();
                usage.setHazardousMaterialId(material.getId());
                usage.setLaboratoryId(material.getLaboratoryId());
                usage.setBatchNo(item.getBatchNo());
                usage.setUsageDate(item.getUsageDate());
                usage.setActionType(item.getActionType());
                usage.setApplicantUserId(requireUser(item.getApplicantUsername()).getId());
                usage.setApproverUserId(optionalUserId(item.getApproverUsername()));
                usage.setOperatorUserId(optionalUserId(item.getOperatorUsername()));
                usage.setQuantity(item.getQuantity());
                usage.setRemainingQuantity(item.getRemainingQuantity());
                usage.setPurpose(item.getPurpose());
                usage.setProjectName(item.getProjectName());
                usage.setWitnessName(item.getWitnessName());
                usage.setUsageStatus(item.getUsageStatus());
                usage.setRemarks(item.getRemarks());
                hazardousUsageMapper.insert(usage);
                continue;
            }
            usage.setApplicantUserId(requireUser(item.getApplicantUsername()).getId());
            usage.setApproverUserId(optionalUserId(item.getApproverUsername()));
            usage.setOperatorUserId(optionalUserId(item.getOperatorUsername()));
            usage.setQuantity(item.getQuantity());
            usage.setRemainingQuantity(item.getRemainingQuantity());
            usage.setPurpose(item.getPurpose());
            usage.setProjectName(item.getProjectName());
            usage.setWitnessName(item.getWitnessName());
            usage.setUsageStatus(item.getUsageStatus());
            usage.setRemarks(item.getRemarks());
            hazardousUsageMapper.updateById(usage);
        }
    }

    private Laboratory requireLaboratory(String laboratoryCode) {
        Laboratory laboratory = laboratoryMapper.selectOne(new LambdaQueryWrapper<Laboratory>()
            .eq(Laboratory::getLaboratoryCode, laboratoryCode)
            .last("LIMIT 1"));
        if (laboratory == null) {
            throw new IllegalStateException("Laboratory not found for sample data: " + laboratoryCode);
        }
        return laboratory;
    }

    private Integer resolveLaboratoryId(String laboratoryCode) {
        if (laboratoryCode == null || laboratoryCode.isBlank()) {
            return null;
        }
        return requireLaboratory(laboratoryCode).getId();
    }

    private EquipmentCategory requireEquipmentCategory(String categoryCode) {
        EquipmentCategory category = equipmentCategoryMapper.selectOne(new LambdaQueryWrapper<EquipmentCategory>()
            .eq(EquipmentCategory::getCategoryCode, categoryCode)
            .last("LIMIT 1"));
        if (category == null) {
            throw new IllegalStateException("Equipment category not found for sample data: " + categoryCode);
        }
        return category;
    }

    private ConsumableCategory requireConsumableCategory(String categoryCode) {
        ConsumableCategory category = consumableCategoryMapper.selectOne(new LambdaQueryWrapper<ConsumableCategory>()
            .eq(ConsumableCategory::getCategoryCode, categoryCode)
            .last("LIMIT 1"));
        if (category == null) {
            throw new IllegalStateException("Consumable category not found for sample data: " + categoryCode);
        }
        return category;
    }

    private Equipment requireEquipment(String equipmentCode) {
        Equipment equipment = equipmentMapper.selectOne(new LambdaQueryWrapper<Equipment>()
            .eq(Equipment::getEquipmentCode, equipmentCode)
            .last("LIMIT 1"));
        if (equipment == null) {
            throw new IllegalStateException("Equipment not found for sample data: " + equipmentCode);
        }
        return equipment;
    }

    private Consumable requireConsumable(String consumableCode) {
        Consumable consumable = consumableMapper.selectOne(new LambdaQueryWrapper<Consumable>()
            .eq(Consumable::getConsumableCode, consumableCode)
            .last("LIMIT 1"));
        if (consumable == null) {
            throw new IllegalStateException("Consumable not found for sample data: " + consumableCode);
        }
        return consumable;
    }

    private HazardousMaterial requireHazardousMaterial(String hazardousCode) {
        HazardousMaterial material = hazardousMaterialMapper.selectOne(new LambdaQueryWrapper<HazardousMaterial>()
            .eq(HazardousMaterial::getHazardousCode, hazardousCode)
            .last("LIMIT 1"));
        if (material == null) {
            throw new IllegalStateException("Hazardous material not found for sample data: " + hazardousCode);
        }
        return material;
    }

    private User requireUser(String username) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
            .eq(User::getUsername, username)
            .last("LIMIT 1"));
        if (user == null) {
            throw new IllegalStateException("User not found for sample data: " + username);
        }
        return user;
    }

    private Integer optionalUserId(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        return requireUser(username).getId();
    }

    private void syncUserRoles(Integer userId, List<String> roleCodes) {
        LinkedHashSet<Integer> roleIds = new LinkedHashSet<>();
        for (String roleCode : safeList(roleCodes)) {
            Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleCode, roleCode)
                .last("LIMIT 1"));
            if (role != null && role.getId() != null) {
                roleIds.add(role.getId());
            }
        }
        if (roleIds.isEmpty()) {
            return;
        }
        for (Integer roleId : roleIds) {
            boolean exists = userRoleMapper.selectCount(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId)
                .eq(UserRole::getRoleId, roleId)) > 0;
            if (exists) {
                continue;
            }
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private Integer resolveInventoryItemId(InventorySeed item) {
        if (Integer.valueOf(2).equals(item.getItemType())) {
            return requireConsumable(item.getItemCode()).getId();
        }
        if (Integer.valueOf(3).equals(item.getItemType())) {
            return requireHazardousMaterial(item.getItemCode()).getId();
        }
        throw new IllegalStateException("Unsupported inventory item type for sample data: " + item.getItemType());
    }

    private <T> List<T> safeList(List<T> items) {
        return items == null ? new ArrayList<>() : items;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DemoDataPayload {

        private List<LaboratorySeed> laboratories;
        private List<CategorySeed> equipmentCategories;
        private List<CategorySeed> consumableCategories;
        private List<EquipmentSeed> equipment;
        private List<ConsumableSeed> consumables;
        private List<HazardousMaterialSeed> hazardousMaterials;
        private List<UserSeed> users;
        private List<InventorySeed> inventory;
        private List<EquipmentBorrowSeed> equipmentBorrows;
        private List<EquipmentRepairSeed> equipmentRepairs;
        private List<EquipmentCalibrationSeed> equipmentCalibrations;
        private List<ConsumableInboundSeed> consumableInbounds;
        private List<ConsumableOutboundSeed> consumableOutbounds;
        private List<HazardousUsageSeed> hazardousUsages;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LaboratorySeed {

        private String laboratoryCode;
        private String laboratoryName;
        private String location;
        private String contactPhone;
        private Integer safetyLevel;
        private Integer status;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CategorySeed {

        private String categoryCode;
        private String categoryName;
        private String description;
        private Integer status;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EquipmentSeed {

        private String laboratoryCode;
        private String categoryCode;
        private String equipmentCode;
        private String equipmentName;
        private String model;
        private String brand;
        private String manufacturer;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime purchaseDate;
        private BigDecimal purchasePrice;
        private Integer serviceLifeYears;
        private String storageLocation;
        private Integer status;
        private Integer calibrationCycleDays;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastCalibrationDate;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime nextCalibrationDate;
        private String remarks;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ConsumableSeed {

        private String laboratoryCode;
        private String categoryCode;
        private String consumableCode;
        private String consumableName;
        private String specification;
        private String unit;
        private BigDecimal unitPrice;
        private BigDecimal safetyStock;
        private BigDecimal maxStock;
        private String storageLocation;
        private Integer expiryRequired;
        private Integer status;
        private String remarks;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HazardousMaterialSeed {

        private String laboratoryCode;
        private String hazardousCode;
        private String materialName;
        private String casNo;
        private String hazardCategory;
        private String specification;
        private String unit;
        private String concentration;
        private String storageLocation;
        private BigDecimal safetyStock;
        private String msdsCode;
        private Integer status;
        private String remarks;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserSeed {

        private String laboratoryCode;
        private String username;
        private String password;
        private String realName;
        private String userNo;
        private String phone;
        private String email;
        private Integer gender;
        private Integer userType;
        private Integer status;
        private List<String> roleCodes;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InventorySeed {

        private String laboratoryCode;
        private Integer itemType;
        private String itemCode;
        private String batchNo;
        private BigDecimal quantity;
        private BigDecimal lockedQuantity;
        private BigDecimal unitPrice;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime productionDate;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime expiryDate;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastStockInTime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastStockOutTime;
        private Integer warningStatus;
        private String remarks;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EquipmentBorrowSeed {

        private String equipmentCode;
        private String borrowerUsername;
        private String approverUsername;
        private String purpose;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime borrowDate;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime dueDate;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime actualReturnDate;
        private Integer borrowStatus;
        private String returnCondition;
        private String remarks;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EquipmentRepairSeed {

        private String equipmentCode;
        private String reporterUsername;
        private String repairUsername;
        private String faultDescription;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime reportTime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime repairStartTime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime repairEndTime;
        private Integer repairStatus;
        private BigDecimal repairCost;
        private String repairResult;
        private String remarks;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EquipmentCalibrationSeed {

        private String equipmentCode;
        private String calibrationUsername;
        private String certificateNo;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime calibrationDate;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime validUntil;
        private Integer calibrationResult;
        private Integer calibrationStatus;
        private String remarks;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ConsumableInboundSeed {

        private String consumableCode;
        private String operatorUsername;
        private String batchNo;
        private Integer inboundType;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalAmount;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime productionDate;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime expiryDate;
        private String supplierName;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime inboundDate;
        private String remarks;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ConsumableOutboundSeed {

        private String consumableCode;
        private String applicantUsername;
        private String approverUsername;
        private String operatorUsername;
        private String batchNo;
        private Integer outboundType;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalAmount;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime outboundDate;
        private String purpose;
        private Integer outboundStatus;
        private String remarks;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HazardousUsageSeed {

        private String hazardousCode;
        private String applicantUsername;
        private String approverUsername;
        private String operatorUsername;
        private Integer actionType;
        private String batchNo;
        private BigDecimal quantity;
        private BigDecimal remainingQuantity;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime usageDate;
        private String purpose;
        private String projectName;
        private String witnessName;
        private Integer usageStatus;
        private String remarks;
    }
}
