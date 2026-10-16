package com.jhy.inspien.scenario2.runner;

import com.jhy.inspien.scenario2.service.ShipmentService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ShipmentExecution {
    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        Path propPath = Path.of("src/main/resources/application.properties");
        if (Files.exists(propPath)) {
            try (var input = Files.newInputStream(propPath)) {
                props.load(input);
            }
        }

        for (String key : props.stringPropertyNames()) {
            String val = props.getProperty(key);
            if (val != null && val.startsWith("${") && val.endsWith("}")) {
                String envKey = val.substring(2, val.length() - 1);
                String envVal = System.getenv(envKey);
                if (envVal != null && !envVal.isBlank()) {
                    props.setProperty(key, envVal);
                }
            }
        }

        String applicantKey = System.getenv("APPLICANT_KEY");
        if (applicantKey == null || applicantKey.isBlank()) {
            applicantKey = props.getProperty("APPLICANT_KEY");
        }
        if (applicantKey == null || applicantKey.isBlank()) {
            throw new IllegalArgumentException("APPLICANT_KEY 값이 설정되지 않았습니다.");
        }

        final String finalApplicantKey = applicantKey;

        ShipmentService shipmentService = new ShipmentService(props);

        // 5분 주기 스케줄러 설정
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("\n[SCHEDULER] 배치 실행 중... (" + java.time.LocalDateTime.now() + ")");
                shipmentService.processUnsentOrders(finalApplicantKey);
            } catch (Exception e) {
                System.err.println("[ERROR] 배치 실행 중 오류 발생: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, 5, TimeUnit.MINUTES);
    }
}