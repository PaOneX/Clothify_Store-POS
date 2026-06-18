package edu.icet.repository.Impl;

import edu.icet.model.dto.NotificationDto;
import edu.icet.repository.NotificationRepository;
import edu.icet.util.CrudUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class NotificationRepositoryImpl implements NotificationRepository {

    @Override
    public void save(String type, String message) {
        CrudUtil.executeUpdate(
                "INSERT INTO notification_log (type, message) VALUES (?,?)",
                type, message
        );
    }

    @Override
    public List<NotificationDto> findActive() {
        return CrudUtil.executeQueryForList(
                "SELECT notification_id, type, message, dismissed, created_at FROM notification_log WHERE dismissed = 0 ORDER BY created_at DESC LIMIT 20",
                this::mapRow
        );
    }

    @Override
    public void dismiss(Integer notificationId) {
        CrudUtil.executeUpdate("UPDATE notification_log SET dismissed = 1 WHERE notification_id = ?", notificationId);
    }

    private NotificationDto mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        return new NotificationDto(
                rs.getInt("notification_id"),
                rs.getString("type"),
                rs.getString("message"),
                rs.getInt("dismissed") == 1,
                ts != null ? ts.toLocalDateTime() : LocalDateTime.now()
        );
    }
}
