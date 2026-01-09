package edu.icet.service;

public interface AuditService {
    void log(Integer userId, String action, String entityType, Integer entityId, String details);
    void logLogin(Integer userId, String username, boolean success);
}
