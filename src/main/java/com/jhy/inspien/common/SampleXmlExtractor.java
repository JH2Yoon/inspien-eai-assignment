package com.jhy.inspien.common;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class SampleXmlExtractor {

    public static void extract(String sampleDataBase64, String outputFileName) throws Exception {
        if (sampleDataBase64 == null || sampleDataBase64.isBlank()) return;

        // Base64 디코딩 + EUC-KR 문자셋 적용 (한글 깨짐 방지)
        byte[] decodedBytes = Base64.getDecoder().decode(sampleDataBase64.trim());
        String xmlContent = new String(decodedBytes, Charset.forName("EUC-KR"));

        // UTF-8 형식의 sample-data.xml 파일로 저장
        Files.writeString(Path.of(outputFileName), xmlContent, StandardCharsets.UTF_8);
    }
}