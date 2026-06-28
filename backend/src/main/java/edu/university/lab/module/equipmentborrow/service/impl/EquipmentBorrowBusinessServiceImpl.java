package edu.university.lab.module.equipmentborrow.service.impl;

import edu.university.lab.auth.security.LoginUser;
import edu.university.lab.auth.security.SecurityUtils;
import edu.university.lab.common.audit.AuditLog;
import edu.university.lab.common.constant.Messages;
import edu.university.lab.module.equipment.entity.Equipment;
import edu.university.lab.module.equipment.service.EquipmentService;
import edu.university.lab.module.equipmentborrow.entity.EquipmentBorrow;
import edu.university.lab.module.equipmentborrow.service.EquipmentBorrowBusinessService;
import edu.university.lab.module.equipmentborrow.service.EquipmentBorrowService;
import edu.university.lab.module.notification.entity.UserNotification;
import edu.university.lab.module.notification.service.UserNotificationService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class EquipmentBorrowBusinessServiceImpl implements EquipmentBorrowBusinessService {

    private final EquipmentBorrowService equipmentBorrowService;

    private final EquipmentService equipmentService;

    private final UserNotificationService userNotificationService;

    @Override
    @AuditLog("借用设备")
    @Transactional(rollbackFor = Exception.class)
    public EquipmentBorrow borrow(EquipmentBorrow request) {
        Equipment equipment = equipmentService.getById(request.getEquipmentId());
        if (equipment == null || equipment.getStatus() == null || equipment.getStatus() != 1) {
            throw new IllegalStateException(Messages.EQUIPMENT_NOT_AVAILABLE);
        }

        if (request.getBorrowDate() == null) {
            request.setBorrowDate(LocalDateTime.now());
        }
        if (request.getDueDate() == null || request.getDueDate().isBefore(request.getBorrowDate())) {
            throw new IllegalStateException(Messages.DUE_DATE_INVALID);
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
            throw new IllegalArgumentException(Messages.BORROW_NOT_FOUND);
        }
        if (borrow.getBorrowStatus() != null && borrow.getBorrowStatus() == 3) {
            throw new IllegalStateException(Messages.ALREADY_RETURNED);
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

    @Override
    @AuditLog("更新借用状态")
    @Transactional(rollbackFor = Exception.class)
    public EquipmentBorrow updateBorrowStatus(Integer borrowId, Integer borrowStatus, String returnCondition, String remarks) {
        EquipmentBorrow borrow = equipmentBorrowService.getById(borrowId);
        if (borrow == null) {
            throw new IllegalArgumentException(Messages.BORROW_NOT_FOUND);
        }

        borrow.setBorrowStatus(borrowStatus);
        borrow.setReturnCondition(returnCondition);
        borrow.setRemarks(remarks);
        if (borrowStatus != null && borrowStatus == 3 && borrow.getActualReturnDate() == null) {
            borrow.setActualReturnDate(LocalDateTime.now());
        }
        if (borrowStatus != null && borrowStatus != 3) {
            borrow.setActualReturnDate(null);
        }
        equipmentBorrowService.updateById(borrow);
        syncEquipmentStatus(borrow);
        return borrow;
    }

    @Override
    @AuditLog("发送逾期催还通知")
    @Transactional(rollbackFor = Exception.class)
    public boolean sendOverdueReminder(Integer borrowId, String message) {
        EquipmentBorrow borrow = equipmentBorrowService.getById(borrowId);
        if (borrow == null) {
            throw new IllegalArgumentException(Messages.BORROW_NOT_FOUND);
        }
        if (borrow.getBorrowStatus() == null || borrow.getBorrowStatus() != 4) {
            throw new IllegalStateException(Messages.ONLY_OVERDUE_CAN_REMIND);
        }

        Equipment equipment = equipmentService.getById(borrow.getEquipmentId());
        String equipmentName = equipment == null ? "设备" : equipment.getEquipmentName();
        UserNotification notification = new UserNotification();
        notification.setReceiverUserId(borrow.getBorrowerUserId());
        notification.setSenderUserId(currentUserId());
        notification.setTitle("设备逾期催还");
        notification.setContent(resolveReminderContent(message, equipmentName, borrow.getDueDate()));
        notification.setNotificationType("equipment_overdue");
        notification.setRelatedType("equipment_borrow");
        notification.setRelatedId(borrow.getId());
        notification.setReadStatus(0);
        userNotificationService.save(notification);
        return true;
    }

    private void syncEquipmentStatus(EquipmentBorrow borrow) {
        Equipment equipment = equipmentService.getById(borrow.getEquipmentId());
        if (equipment == null || borrow.getBorrowStatus() == null) {
            return;
        }
        if (borrow.getBorrowStatus() == 3 || borrow.getBorrowStatus() == 5) {
            equipment.setStatus(1);
        } else if (borrow.getBorrowStatus() == 2 || borrow.getBorrowStatus() == 4) {
            equipment.setStatus(2);
        }
        equipmentService.updateById(equipment);
    }

    private Integer currentUserId() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        return loginUser == null || loginUser.getUser() == null ? null : loginUser.getUser().getId();
    }

    private String resolveReminderContent(String message, String equipmentName, LocalDateTime dueDate) {
        if (StringUtils.hasText(message)) {
            return message.trim();
        }
        return "您借用的设备“" + equipmentName + "”已超过应还时间（" + dueDate + "），请尽快归还或联系管理员处理。";
    }
}
