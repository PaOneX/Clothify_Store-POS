package edu.icet.service.Impl;

import edu.icet.repository.AuditRepository;
import edu.icet.service.AuditService;

public class AuditServiceImpl implements AuditService {

    private final AuditRepository auditRepository;

    public AuditServiceImpl(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @Override
    public void log(Integer userId, String action, String entityType, Integer entityId, String details) {
        auditRepository.log(userId, action, entityType, entityId, details);
    }

    @Override
    public void logLogin(Integer userId, String username, boolean success) {
        auditRepository.log(userId, success ? "LOGIN" : "LOGIN_FAILED", "user", userId,
                success ? "Successful login: " + username : "Failed login: " + username);
    }
}
