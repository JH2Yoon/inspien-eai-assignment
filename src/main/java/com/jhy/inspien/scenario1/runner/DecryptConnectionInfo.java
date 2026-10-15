package com.jhy.inspien.scenario1.runner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jhy.inspien.common.ConnectionInfoDecryptor;
import com.jhy.inspien.common.SampleXmlExtractor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

public class DecryptConnectionInfo {
    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("휴대폰 번호 입력 (예: 010-1234-5678): ");
        String phoneNumber = reader.readLine();

        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("휴대폰 번호를 입력해 주세요.");
        }

        char[] phoneChars = phoneNumber.toCharArray();
        try {
            ObjectMapper mapper = new ObjectMapper();
            Path responsePath = Path.of("response.json");
            if (!Files.exists(responsePath)) {
                throw new IllegalStateException("response.json 파일이 존재하지 않습니다. 프로젝트 루트에 넣어주세요.");
            }

            JsonNode response = mapper.readTree(Files.readString(responsePath, StandardCharsets.UTF_8));
            ObjectNode result = mapper.createObjectNode();

            // 1. 지원자 키 추출
            result.put("APPLICANT_KEY", response.path("APPLICANT_KEY").asText(""));

            // 2. DB 및 FTP 접속 정보 복호화
            String[] connectionNames = {"ORDER_TB_CONN", "SHIPMENT_TB_CONN", "FTP_CONN"};
            for (String connectionName : connectionNames) {
                JsonNode encryptedConnection = response.path(connectionName);
                if (!encryptedConnection.isObject()) continue;

                ObjectNode decryptedConnection = mapper.createObjectNode();
                Iterator<Map.Entry<String, JsonNode>> fields = encryptedConnection.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    String decryptedValue = ConnectionInfoDecryptor.decrypt(field.getValue().asText(), phoneNumber);
                    decryptedConnection.put(field.getKey(), decryptedValue);
                }
                result.set(connectionName, decryptedConnection);
            }

            // 복호화 결과 파일 생성
            Path output = Path.of("decrypted-connections.json");
            Files.writeString(output, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
            System.out.println("접속 정보 복호화 완료: " + output.toAbsolutePath());

            // 3. common 유틸을 통한 샘플 XML 자동 추출 (EUC-KR 디코딩 적용)
            JsonNode sampleDataNode = response.path("SAMPLE_DATA");
            if (!sampleDataNode.isMissingNode() && !sampleDataNode.asText().isBlank()) {
                SampleXmlExtractor.extract(sampleDataNode.asText(), "sample-data.xml");
                System.out.println("샘플 XML 파일 추출 완료 (EUC-KR 적용): " + Path.of("sample-data.xml").toAbsolutePath());
            } else {
                System.out.println("ℹ안내: response.json에 SAMPLE_DATA 필드가 없습니다.");
            }

        } finally {
            Arrays.fill(phoneChars, '\0');
        }
    }
}