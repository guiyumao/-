package edu.university.lab.module.equipmentborrow.service.impl;

import edu.university.lab.common.audit.AuditLog;
import edu.university.lab.module.equipment.entity.Equipment;
import edu.university.lab.module.equipment.service.EquipmentService;
import edu.university.lab.module.equipmentborrow.entity.EquipmentBorrow;
import edu.university.lab.module.equipmentborrow.service.EquipmentBorrowBusinessService;
import edu.university.lab.module.equipmentborrow.service.EquipmentBorrowService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EquipmentBorrowBusinessServiceImpl implements EquipmentBorrowBusinessService {

    private final EquipmentBorrowService equipmentBorrowService;

    private final EquipmentService equipmentService;

    @Override
    @AuditLog("借用设备")
    @Transactional(rollbackFor = Exception.class)
    public EquipmentBorrow borrow(EquipmentBorrow request) {
        Equipment equipment = equipmentService.getById(request.getEquipmentId());
        if (equipment == null || equipment.getStatus() == null || equipment.getStatus() != 1) {
            throw new IllegalStateException("Equipment is not available for borrowing");
        }

        if (request.getBorrowDate() == null) {
            request.setBorrowDate(LocalDateTime.now());
        }
        if (request.getDueDate() == null || request.getDueDate().isBefore(request.getBorrowDate())) {
            throw new IllegalStateException("Due date must be later than borrow date");
        }
        request.setBorrowStatus(2);
        equipmentBorrowService.save(request);
        equipment.setStatus(2);
        equipmentService.updateById(equipment);
        return request;
    }

    @Override
    @AuditLog("归还设备")
    @Transactional(rollbackFor = Exception.class)
    public boolean returnEquipment(Integer borrowId, String returnCondition, String remarks) {
        EquipmentBorrow borrow = equipmentBorrowService.getById(borrowId);
        if (borrow == null) {
            throw new IllegalArgumentException("Borrow record not found");
        }
        if (borrow.getBorrowStatus() != null && borrow.getBorrowStatus() == 3) {
            throw new IllegalStateException("Equipment has already been returned");
        }

        borrow.setBorrowStatus(3);
        borrow.setReturnCondition(returnCondition);
        borrow.setRemarks(remarks);
        borrow.setActualReturnDate(LocalDateTime.now());
        equipmentBorrowService.updateById(borrow);

        Equipment equipment = equipmentService.getById(borrow.getEquipmentId());
        if (equipment != null) {
            equipment.setStatus(1);
            equipmentService.updateById(equipment);
        }
        return true;
    }
}
