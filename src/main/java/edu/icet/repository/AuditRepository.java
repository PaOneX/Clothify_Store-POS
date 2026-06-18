package edu.icet.repository;

public interface AuditRepository {
    void log(Integer userId, String action, String entityType, Integer entityId, String details);
}
