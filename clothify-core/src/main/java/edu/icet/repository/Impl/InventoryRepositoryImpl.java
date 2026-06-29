package edu.icet.repository.Impl;

import edu.icet.model.dto.InventoryLogDto;
import edu.icet.model.enums.InventoryReason;
import edu.icet.db.DBConnection;
import edu.icet.repository.InventoryRepository;
import edu.icet.util.CrudUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class InventoryRepositoryImpl implements InventoryRepository {

    private static final String BASE_SELECT = """
            SELECT l.log_id, l.variant_id, p.product_name, v.size, v.color,
                   l.change_qty, l.reason, l.user_id, u.username, l.created_at
            FROM inventory_log l
            JOIN product_variant v ON l.variant_id = v.variant_id
            JOIN product p ON v.product_id = p.product_id
            LEFT JOIN `user` u ON l.user_id = u.user_id
            """;

    @Override
    public void logChange(Integer variantId, int changeQty, InventoryReason reason, Integer userId) {
        try (Connection connection = DBConnection.getInstance().getConnection()) {
            logChange(connection, variantId, changeQty, reason, userId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection", e);
        }
    }

    @Override
    public void logChange(Connection connection, Integer variantId, int changeQty, InventoryReason reason, Integer userId) {
        CrudUtil.executeUpdate(connection,
                "INSERT INTO inventory_log (variant_id, change_qty, reason, user_id) VALUES (?,?,?,?)",
                variantId,
                changeQty,
                reason.name(),
                userId
        );
    }

    @Override
    public List<InventoryLogDto> getLogs() {
        return CrudUtil.executeQueryForList(BASE_SELECT + " ORDER BY l.created_at DESC", this::mapRow);
    }

    @Override
    public List<InventoryLogDto> getLogsByVariant(Integer variantId) {
        return CrudUtil.executeQueryForList(
                BASE_SELECT + " WHERE l.variant_id = ? ORDER BY l.created_at DESC",
                this::mapRow,
                variantId
        );
    }

    private InventoryLogDto mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        return new InventoryLogDto(
                rs.getInt("log_id"),
                rs.getInt("variant_id"),
                rs.getString("product_name"),
                rs.getString("size"),
                rs.getString("color"),
                rs.getInt("change_qty"),
                rs.getString("reason"),
                rs.getObject("user_id") != null ? rs.getInt("user_id") : null,
                rs.getString("username"),
                ts != null ? ts.toLocalDateTime() : LocalDateTime.now()
        );
    }
}
