package edu.university.lab.module.auditlog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.university.lab.module.auditlog.dto.AuditLogQuery;
import edu.university.lab.module.auditlog.entity.SysAuditLog;
import edu.university.lab.module.auditlog.mapper.SysAuditLogMapper;
import edu.university.lab.module.auditlog.service.SysAuditLogService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SysAuditLogServiceImpl extends ServiceImpl<SysAuditLogMapper, SysAuditLog> implements SysAuditLogService {

    @Override
    public Page<SysAuditLog> pageQuery(AuditLogQuery query) {
        LambdaQueryWrapper<SysAuditLog> wrapper = new LambdaQueryWrapper<SysAuditLog>()
            .orderByDesc(SysAuditLog::getCreateTime)
            .orderByDesc(SysAuditLog::getId);
        if (query.getStartDate() != null) {
            wrapper.ge(SysAuditLog::getCreateTime, query.getStartDate());
        }
        if (query.getEndDate() != null) {
            wrapper.le(SysAuditLog::getCreateTime, query.getEndDate());
        }
        return page(new Page<>(query.getCurrent(), query.getPageSize()), wrapper);
    }

    @Override
    @Async
    public void saveAsync(SysAuditLog auditLog) {
        save(auditLog);
    }
}
