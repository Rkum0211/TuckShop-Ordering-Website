package com.tuckshop.ordering.service;

import com.tuckshop.ordering.entity.AuditLog;
import com.tuckshop.ordering.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getLogs(int page, int size, String action, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return auditLogRepository.findFilteredLogs(action, search, pageable);
    }

    @Transactional
    public void logAction(String username, String action, String entityType, String entityId, String description, String ipAddress) {
        AuditLog log = new AuditLog();
        log.setAdminUsername(username);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDescription(description);
        log.setIpAddress(ipAddress);
        auditLogRepository.save(log);
    }
}
 