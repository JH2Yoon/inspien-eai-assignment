package com.jhy.inspien.scenario1.repository;

import com.jhy.inspien.scenario1.dto.ParsedOrder;
import com.jhy.inspien.scenario1.dto.OrderRecord;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

public class OrderRepository {
    private final String url, username, password, tableName;

    public OrderRepository(Properties props) {
        this.url = resolve(props, "order.db.url", "ORDER_DB_URL");
        this.username = resolve(props, "order.db.username", "ORDER_DB_ID");
        this.password = resolve(props, "order.db.password", "ORDER_DB_PASSWORD");
        this.tableName = resolve(props, "order.db.table", "ORDER_DB_TABLE");
    }

    private String resolve(Properties props, String propKey, String envKey) {
        String val = System.getenv(envKey);
        if (val != null && !val.isBlank()) return val;
        return props.getProperty(propKey, "");
    }

    public void saveAll(List<OrderRecord> records, String applicantKey) throws Exception {
        String targetTable = (tableName != null && !tableName.isBlank()) ? tableName : "ORDER_TB";

        String sql = String.format(
                "INSERT INTO %s (ORDER_ID, APPLICANT_KEY, USER_ID, ITEM_ID, NAME, ADDRESS, ITEM_NAME, PRICE, STATUS, CREATE_TIME) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                targetTable
        );

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (OrderRecord rec : records) {
                    ParsedOrder o = rec.getParsedOrder();
                    pstmt.setString(1, rec.getOrderId());
                    pstmt.setString(2, applicantKey);
                    pstmt.setString(3, o.getHeader().getUserId());
                    pstmt.setString(4, o.getItem().getItemId());
                    pstmt.setString(5, o.getHeader().getName());
                    pstmt.setString(6, o.getHeader().getAddress());
                    pstmt.setString(7, o.getItem().getItemName());
                    pstmt.setString(8, o.getItem().getPrice().toString());
                    pstmt.setString(9, o.getHeader().getStatus());
                    pstmt.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                if (e.getErrorCode() == 1) {
                    System.out.println("[안내] 이미 존재하는 데이터(ORDER_ID 중복)이 감지되어 저장을 건너뛰고 다음 단계(FTP 업로드)를 진행합니다.");
                    conn.rollback();
                } else {
                    conn.rollback();
                    throw e;
                }
            }
        }
    }
}