package edu.university.lab.module.equipmentborrow.service;

import edu.university.lab.module.equipmentborrow.entity.EquipmentBorrow;

public interface EquipmentBorrowBusinessService {

    EquipmentBorrow borrow(EquipmentBorrow request);

    boolean returnEquipment(Integer borrowId, String returnCondition, String remarks);
}
