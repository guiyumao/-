package edu.university.lab.module.auditlog.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("sys_audit_log")
public class SysAuditLog {

    @TableId
    private Integer id;

    private Integer userId;

    private String username;

    private String operation;

    private String method;

    private String params;

    private String ip;

    private LocalDateTime createTime;
}
