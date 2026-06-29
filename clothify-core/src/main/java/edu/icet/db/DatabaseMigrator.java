package edu.icet.db;

import edu.icet.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseMigrator {

    private static final Logger log = LoggerFactory.getLogger(DatabaseMigrator.class);
    private static final String V3_ONLINE_ORDERS = "V3_online_orders";

    private DatabaseMigrator() {
    }

    public static void migrate() {
        try (Connection connection = DriverManager.getConnection(
                AppConfig.getDbUrl(),
                AppConfig.getDbUser(),
                AppConfig.getDbPassword())) {
            ensureMigrationTable(connection);
            if (!isApplied(connection, V3_ONLINE_ORDERS)) {
                applyV3OnlineOrders(connection);
                markApplied(connection, V3_ONLINE_ORDERS);
                log.info("Applied database migration {}", V3_ONLINE_ORDERS);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database migration failed", e);
        }
    }

    private static void ensureMigrationTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS schema_migration (
                        version    VARCHAR(50) PRIMARY KEY,
                        applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
        }
    }

    private static boolean isApplied(Connection connection, String version) throws SQLException {
        try (var ps = connection.prepareStatement(
                "SELECT 1 FROM schema_migration WHERE version = ?")) {
            ps.setString(1, version);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void markApplied(Connection connection, String version) throws SQLException {
        try (var ps = connection.prepareStatement(
                "INSERT INTO schema_migration (version) VALUES (?)")) {
            ps.setString(1, version);
            ps.executeUpdate();
        }
    }

    private static void applyV3OnlineOrders(Connection connection) throws SQLException {
        if (!hasColumn(connection, "order_header", "order_source")) {
            execute(connection, """
                    ALTER TABLE order_header
                        ADD COLUMN order_source ENUM('POS', 'ONLINE') NOT NULL DEFAULT 'POS' AFTER status
                    """);
        }

        execute(connection, """
                ALTER TABLE order_header
                    MODIFY COLUMN status ENUM('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'COMPLETED'
                """);

        execute(connection, """
                INSERT INTO employee (name, phone, email, address, hire_date, active)
                SELECT 'Web Orders', '0000000000', 'web@clothify.com', 'Online', CURDATE(), 1
                WHERE NOT EXISTS (SELECT 1 FROM employee WHERE name = 'Web Orders')
                """);

        execute(connection, """
                INSERT INTO `user` (username, password_hash, role, employee_id)
                SELECT 'web_orders', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'STAFF',
                       (SELECT employee_id FROM employee WHERE name = 'Web Orders' LIMIT 1)
                WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE username = 'web_orders')
                """);
    }

    private static boolean hasColumn(Connection connection, String table, String column) throws SQLException {
        try (var ps = connection.prepareStatement("""
                SELECT 1
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = ?
                """)) {
            ps.setString(1, table);
            ps.setString(2, column);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
