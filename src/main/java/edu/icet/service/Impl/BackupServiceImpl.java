package edu.icet.service.Impl;

import edu.icet.config.AppConfig;
import edu.icet.service.BackupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BackupServiceImpl implements BackupService {

    private static final Logger log = LoggerFactory.getLogger(BackupServiceImpl.class);

    @Override
    public File createBackup() throws Exception {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path backupDir = Path.of(AppConfig.getBackupDir());
        Files.createDirectories(backupDir);
        File backupFile = backupDir.resolve("clothify_backup_" + timestamp + ".sql").toFile();

        String dbName = extractDbName(AppConfig.getDbUrl());
        ProcessBuilder pb = new ProcessBuilder(
                AppConfig.getMysqlDumpPath(),
                "-u", AppConfig.getDbUser(),
                "-p" + AppConfig.getDbPassword(),
                dbName
        );
        pb.redirectOutput(backupFile);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        Process process = pb.start();
        int exit = process.waitFor();
        if (exit != 0) {
            throw new RuntimeException("Backup failed with exit code " + exit);
        }
        log.info("Backup created: {}", backupFile.getAbsolutePath());
        return backupFile;
    }

    @Override
    public void restoreBackup(File file) throws Exception {
        String dbName = extractDbName(AppConfig.getDbUrl());
        ProcessBuilder pb = new ProcessBuilder(
                AppConfig.getMysqlPath(),
                "-u", AppConfig.getDbUser(),
                "-p" + AppConfig.getDbPassword(),
                dbName
        );
        pb.redirectInput(file);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        Process process = pb.start();
        int exit = process.waitFor();
        if (exit != 0) {
            throw new RuntimeException("Restore failed with exit code " + exit);
        }
        log.info("Backup restored from: {}", file.getAbsolutePath());
    }

    @Override
    public void scheduleAutoBackup() {
        int hours = AppConfig.getBackupIntervalHours();
        if (hours <= 0) return;
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "auto-backup");
            t.setDaemon(true);
            return t;
        }).scheduleAtFixedRate(() -> {
            try {
                createBackup();
            } catch (Exception e) {
                log.error("Auto backup failed", e);
            }
        }, hours, hours, TimeUnit.HOURS);
    }

    private String extractDbName(String url) {
        int slash = url.lastIndexOf('/');
        String part = url.substring(slash + 1);
        int q = part.indexOf('?');
        return q > 0 ? part.substring(0, q) : part;
    }
}
