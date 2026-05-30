package edu.university.lab.common.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.university.lab.auth.security.LoginUser;
import edu.university.lab.auth.security.SecurityUtils;
import edu.university.lab.module.auditlog.entity.SysAuditLog;
import edu.university.lab.module.auditlog.service.SysAuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final ObjectMapper objectMapper;

    private final SysAuditLogService sysAuditLogService;

    @AfterReturning("@annotation(auditLog)")
    public void afterReturning(JoinPoint joinPoint, AuditLog auditLog) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        HttpServletRequest request = currentRequest();

        SysAuditLog entity = new SysAuditLog();
        if (loginUser != null) {
            entity.setUserId(loginUser.getUser().getId());
            entity.setUsername(loginUser.getUsername());
        }
        entity.setOperation(auditLog.value());
        entity.setMethod(joinPoint.getSignature().toShortString());
        entity.setParams(serializeParams(joinPoint.getArgs()));
        entity.setIp(request == null ? "unknown" : resolveClientIp(request));
        entity.setCreateTime(LocalDateTime.now());
        sysAuditLogService.saveAsync(entity);
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest();
        }
        return null;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String serializeParams(Object[] args) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("args", args);
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            return "{\"args\":\"unserializable\"}";
        }
    }
}
