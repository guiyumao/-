package edu.university.lab.module.auditlog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import edu.university.lab.module.auditlog.dto.AuditLogQuery;
import edu.university.lab.module.auditlog.entity.SysAuditLog;

public interface SysAuditLogService extends IService<SysAuditLog> {

    Page<SysAuditLog> pageQuery(AuditLogQuery query);

    void saveAsync(SysAuditLog auditLog);
}
