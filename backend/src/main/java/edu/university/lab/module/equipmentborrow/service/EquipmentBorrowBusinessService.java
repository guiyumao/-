package edu.university.lab.module.equipmentborrow.service;

import edu.university.lab.module.equipmentborrow.entity.EquipmentBorrow;

public interface EquipmentBorrowBusinessService {

    EquipmentBorrow borrow(EquipmentBorrow request);

    boolean returnEquipment(Integer borrowId, String returnCondition, String remarks);

    EquipmentBorrow updateBorrowStatus(Integer borrowId, Integer borrowStatus, String returnCondition, String remarks);

    boolean sendOverdueReminder(Integer borrowId, String message);
}
