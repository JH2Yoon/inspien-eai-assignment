package com.jhy.inspien.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogWriter {
    private static final String LOG_FILE_PATH = "logs/execution-history.log";

    public static synchronized void writeLog(String scenario, String status, String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logEntry = String.format("[%s] [%s] [%s] %s%n", timestamp, scenario, status, message);

        try {
            Path path = Path.of(LOG_FILE_PATH);
            if (path.getParent() != null && !Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            Files.writeString(path, logEntry, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("[ERROR] 로그 파일 저장 실패: " + e.getMessage());
        }
    }
}