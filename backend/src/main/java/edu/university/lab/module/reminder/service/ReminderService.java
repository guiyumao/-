package edu.university.lab.module.reminder.service;

import edu.university.lab.module.reminder.dto.ReminderResponse;

public interface ReminderService {

    ReminderResponse listReminders();

    long countPendingCalibrations();

    long countExpiringConsumables();
}
