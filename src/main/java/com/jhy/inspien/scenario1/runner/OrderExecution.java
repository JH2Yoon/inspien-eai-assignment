package com.jhy.inspien.scenario1.runner;

import com.jhy.inspien.scenario1.service.OrderService;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class OrderExecution {
    public static void main(String[] args) throws Exception {
        Path xmlPath = Path.of("sample-data.xml");
        if (!Files.exists(xmlPath)) {
            throw new IllegalStateException("sample-data.xml 파일이 존재하지 않습니다.");
        }
        String xml = "<ROOT>" + Files.readString(xmlPath, StandardCharsets.UTF_8) + "</ROOT>";

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

        String participantName = System.getenv("PARTICIPANT_NAME");
        if (participantName == null || participantName.isBlank() || participantName.contains("???")) {
            participantName = props.getProperty("PARTICIPANT_NAME");
        }

        String applicantKey = System.getenv("APPLICANT_KEY");
        if (applicantKey == null || applicantKey.isBlank() || applicantKey.contains("???")) {
            applicantKey = props.getProperty("APPLICANT_KEY");
        }
        if (applicantKey == null || applicantKey.isBlank()) {
            throw new IllegalArgumentException("APPLICANT_KEY 값이 설정되지 않았습니다.");
        }

        OrderService orderService = new OrderService(props, participantName);
        orderService.processOrders(xml, applicantKey);
    }
}