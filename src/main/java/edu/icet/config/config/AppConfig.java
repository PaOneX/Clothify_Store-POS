package edu.icet.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class AppConfig {

    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                throw new IllegalStateException("db.properties not found on classpath");
            }
            PROPERTIES.load(input);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private AppConfig() {
    }

    public static String getDbUrl() {
        return PROPERTIES.getProperty("db.url");
    }

    public static String getDbUser() {
        return PROPERTIES.getProperty("db.user");
    }

    public static String getDbPassword() {
        return PROPERTIES.getProperty("db.password");
    }

    public static double getTaxRate() {
        return Double.parseDouble(PROPERTIES.getProperty("app.tax.rate", "0.0"));
    }

    public static int getLowStockThreshold() {
        return Integer.parseInt(PROPERTIES.getProperty("app.low.stock.threshold", "5"));
    }

    public static String getStoreName() {
        return PROPERTIES.getProperty("app.store.name", "Clothify Store");
    }

    public static int getSessionTimeoutMinutes() {
        return Integer.parseInt(PROPERTIES.getProperty("app.session.timeout.minutes", "30"));
    }

    public static String getBackupDir() {
        return PROPERTIES.getProperty("app.backup.dir", "backups");
    }

    public static int getBackupIntervalHours() {
        return Integer.parseInt(PROPERTIES.getProperty("app.backup.interval.hours", "0"));
    }

    public static String getMysqlDumpPath() {
        return PROPERTIES.getProperty("app.mysql.dump.path", "mysqldump");
    }

    public static String getMysqlPath() {
        return PROPERTIES.getProperty("app.mysql.path", "mysql");
    }

    public static String getUploadsDir() {
        return PROPERTIES.getProperty("app.uploads.dir", "data/uploads");
    }

    public static String getApiKey() {
        return PROPERTIES.getProperty("app.api.key", "clothify-dev-key");
    }

    public static String getWebCashierUsername() {
        return PROPERTIES.getProperty("app.web.cashier.username", "web_orders");
    }
}
