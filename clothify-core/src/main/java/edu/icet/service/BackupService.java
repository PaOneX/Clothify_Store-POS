package edu.icet.service;

import java.io.File;

public interface BackupService {
    File createBackup() throws Exception;
    void restoreBackup(File file) throws Exception;
    void scheduleAutoBackup();
}
