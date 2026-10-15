package com.jhy.inspien.scenario1.file;

import com.jhy.inspien.scenario1.dto.ParsedOrder;
import com.jhy.inspien.scenario1.dto.OrderRecord;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

public class OrderFileService {
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String remotePath;
    private final String participantName;

    public OrderFileService(Properties props, String participantName) {
        this.host = resolve(props, "ftp.url", "FTP_URL").replaceFirst("^ftp://", "").replaceFirst("/+$", "");
        String portStr = resolve(props, "ftp.port", "FTP_PORT");
        this.port = (portStr != null && !portStr.isBlank()) ? Integer.parseInt(portStr) : 21;
        this.username = resolve(props, "ftp.username", "FTP_ID");
        this.password = resolve(props, "ftp.password", "FTP_PASSWORD");
        this.remotePath = resolve(props, "ftp.path", "FTP_PATH");

        String name = participantName;

        if (name == null || name.isBlank() || name.contains("???")) {
            name = System.getenv("PARTICIPANT_NAME");
        }
        if (name == null || name.isBlank() || name.contains("???")) {
            name = props.getProperty("PARTICIPANT_NAME");
        }

        this.participantName = name;
    }

    private String resolve(Properties props, String propKey, String envKey) {
        String val = System.getenv(envKey);
        if (val != null && !val.isBlank()) return val;
        return props.getProperty(propKey, "");
    }

    public String upload(List<OrderRecord> records, String applicantKey) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (OrderRecord rec : records) {
            ParsedOrder o = rec.getParsedOrder();
            sb.append(rec.getOrderId()).append("^")
                    .append(o.getHeader().getUserId()).append("^")
                    .append(o.getItem().getItemId()).append("^")
                    .append(applicantKey).append("^")
                    .append(o.getHeader().getName()).append("^")
                    .append(o.getHeader().getAddress()).append("^")
                    .append(o.getItem().getItemName()).append("^")
                    .append(o.getItem().getPrice()).append("\n");
        }

        String fileName = "INSPIEN_" + participantName + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".txt";

        FTPClient ftp = new FTPClient();
        try {
            ftp.connect(host, port);
            if (!ftp.login(username, password)) throw new IllegalStateException("FTP 로그인 실패");
            ftp.enterLocalPassiveMode();
            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            // FTP 제어 채널 UTF-8 설정
            ftp.setControlEncoding("UTF-8");

            if (remotePath != null && !remotePath.isBlank() && !ftp.changeWorkingDirectory(remotePath)) {
                throw new IllegalStateException("FTP 경로 이동 실패");
            }

            try (ByteArrayInputStream in = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8))) {
                String encodedFileName = new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);

                if (!ftp.storeFile(encodedFileName, in)) {
                    throw new IllegalStateException("FTP 업로드 실패");
                }
            }
            return fileName;
        } finally {
            if (ftp.isConnected()) { ftp.logout(); ftp.disconnect(); }
        }
    }

    public void delete(String fileName) throws Exception {
        FTPClient ftp = new FTPClient();
        try {
            ftp.connect(host, port);
            ftp.login(username, password);
            ftp.enterLocalPassiveMode();
            if (remotePath != null && !remotePath.isBlank()) ftp.changeWorkingDirectory(remotePath);

            String encodedFileName = new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
            ftp.deleteFile(encodedFileName);
        } finally {
            if (ftp.isConnected()) { ftp.logout(); ftp.disconnect(); }
        }
    }
}