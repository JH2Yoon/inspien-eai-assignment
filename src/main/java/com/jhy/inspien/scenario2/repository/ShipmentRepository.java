package com.jhy.inspien.scenario2.repository;

import com.jhy.inspien.common.OrderIdGenerator;
import com.jhy.inspien.scenario2.dto.ShipmentRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ShipmentRepository {
    private final String orderUrl, orderUser, orderPass, orderTable;
    private final String shipmentUrl, shipmentUser, shipmentPass, shipmentTable;

    public ShipmentRepository(Properties props) {
        this.orderUrl = resolve(props, "order.db.url", "ORDER_DB_URL");
        this.orderUser = resolve(props, "order.db.username", "ORDER_DB_ID");
        this.orderPass = resolve(props, "order.db.password", "ORDER_DB_PASSWORD");
        this.orderTable = resolve(props, "order.db.table", "ORDER_DB_TABLE");

        this.shipmentUrl = resolve(props, "shipment.db.url", "SHIPMENT_DB_URL");
        this.shipmentUser = resolve(props, "shipment.db.username", "SHIPMENT_DB_ID");
        this.shipmentPass = resolve(props, "shipment.db.password", "SHIPMENT_DB_PASSWORD");
        this.shipmentTable = resolve(props, "shipment.db.table", "SHIPMENT_DB_TABLE");
    }

    private String resolve(Properties props, String propKey, String envKey) {
        String val = System.getenv(envKey);
        if (val != null && !val.isBlank()) return val;
        return props.getProperty(propKey, "");
    }

    // 1. STATUS = 'N'인 미전송 주문 조회 (ADDRESS 컬럼 포함)
    public List<ShipmentRecord> findUnsentOrders(String applicantKey) throws Exception {
        String targetTable = (orderTable != null && !orderTable.isBlank()) ? orderTable : "ORDER_TB";
        String sql = "SELECT ORDER_ID, ITEM_ID, ADDRESS FROM " + targetTable +
                " WHERE APPLICANT_KEY = ? AND (STATUS = 'N' OR STATUS IS NULL)";

        List<ShipmentRecord> records = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(orderUrl, orderUser, orderPass);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, applicantKey);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String orderId = rs.getString("ORDER_ID");
                    String shipmentId = OrderIdGenerator.generate();
                    String itemId = rs.getString("ITEM_ID");
                    String address = rs.getString("ADDRESS");

                    records.add(new ShipmentRecord(orderId, shipmentId, applicantKey, itemId, address));
                }
            }
        }
        return records;
    }

    // 2. SHIPMENT_TB 적재 및 ORDER_TB 상태 'Y' 업데이트
    public void processShipmentBatch(List<ShipmentRecord> records) throws Exception {
        String targetShipmentTable = (shipmentTable != null && !shipmentTable.isBlank()) ? shipmentTable : "SHIPMENT_TB";
        String targetOrderTable = (orderTable != null && !orderTable.isBlank()) ? orderTable : "ORDER_TB";

        // 명세서 컬럼 구조에 맞춘 INSERT 쿼리 (SHIPMENT_ID, APPLICANT_KEY, ORDER_ID, ITEM_ID, ADDRESS)
        String insertSql = String.format(
                "INSERT INTO %s (SHIPMENT_ID, APPLICANT_KEY, ORDER_ID, ITEM_ID, ADDRESS) VALUES (?, ?, ?, ?, ?)",
                targetShipmentTable
        );

        String updateSql = String.format(
                "UPDATE %s SET STATUS = 'Y' WHERE ORDER_ID = ? AND APPLICANT_KEY = ?",
                targetOrderTable
        );

        try (Connection shipConn = DriverManager.getConnection(shipmentUrl, shipmentUser, shipmentPass);
             Connection orderConn = DriverManager.getConnection(orderUrl, orderUser, orderPass)) {

            shipConn.setAutoCommit(false);
            orderConn.setAutoCommit(false);

            try (PreparedStatement insertStmt = shipConn.prepareStatement(insertSql);
                 PreparedStatement updateStmt = orderConn.prepareStatement(updateSql)) {

                for (ShipmentRecord rec : records) {
                    try {
                        insertStmt.setString(1, rec.getShipmentId());
                        insertStmt.setString(2, rec.getApplicantKey());
                        insertStmt.setString(3, rec.getOrderId());
                        insertStmt.setString(4, rec.getItemId());
                        insertStmt.setString(5, rec.getAddress());
                        insertStmt.executeUpdate();

                        // 성공한 건에 한해 ORDER_TB 상태 'Y'로 업데이트
                        updateStmt.setString(1, rec.getOrderId());
                        updateStmt.setString(2, rec.getApplicantKey());
                        updateStmt.executeUpdate();

                    } catch (SQLException e) {
                        if (e.getErrorCode() == 1) {
                            System.out.println("[안내] 이미 SHIPMENT_TB에 존재하는 중복 키입니다: " + rec.getOrderId());
                        } else {
                            throw e;
                        }
                    }
                }

                shipConn.commit();
                orderConn.commit();
            } catch (Exception e) {
                shipConn.rollback();
                orderConn.rollback();
                throw e;
            }
        }
    }
}