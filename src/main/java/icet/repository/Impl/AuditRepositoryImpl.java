package edu.icet.repository.Impl;

import edu.icet.repository.AuditRepository;
import edu.icet.util.CrudUtil;

public class AuditRepositoryImpl implements AuditRepository {

    @Override
    public void log(Integer userId, String action, String entityType, Integer entityId, String details) {
        CrudUtil.executeUpdate(
                "INSERT INTO audit_log (user_id, action, entity_type, entity_id, details) VALUES (?,?,?,?,?)",
                userId, action, entityType, entityId, details
        );
    }
}
