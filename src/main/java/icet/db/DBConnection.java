package edu.icet.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import edu.icet.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class DBConnection {

    private static final Logger log = LoggerFactory.getLogger(DBConnection.class);
    private static DBConnection instance;
    private final HikariDataSource dataSource;

    private DBConnection() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(AppConfig.getDbUrl());
        config.setUsername(AppConfig.getDbUser());
        config.setPassword(AppConfig.getDbPassword());
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setPoolName("ClothifyPool");
        dataSource = new HikariDataSource(config);
        log.info("HikariCP connection pool initialized");
    }

    public static DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public Connection getConnectionUnchecked() {
        try {
            return getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection", e);
        }
    }
}
