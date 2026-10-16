package com.jhy.inspien.scenario1.service;

import com.jhy.inspien.common.LogWriter;
import com.jhy.inspien.common.OrderIdGenerator;
import com.jhy.inspien.common.XmlParser;
import com.jhy.inspien.scenario1.dto.ParsedOrder;
import com.jhy.inspien.scenario1.dto.OrderRecord;
import com.jhy.inspien.scenario1.file.OrderFileService;
import com.jhy.inspien.scenario1.repository.OrderRepository;

import java.util.*;

public class OrderService {
    private final OrderFileService fileService;
    private final OrderRepository orderRepository;

    public OrderService(Properties props, String participantName) {
        this.fileService = new OrderFileService(props, participantName);
        this.orderRepository = new OrderRepository(props);
    }

    public void processOrders(String xml, String applicantKey) throws Exception {
        String scenarioName = "SCENARIO_1";
        LogWriter.writeLog(scenarioName, "START", "주문 정보 파싱 및 연계 작업을 시작합니다.");

        try {
            List<ParsedOrder> parsedOrders = XmlParser.parseOrders(xml);
            List<OrderRecord> records = new ArrayList<>();
            Set<String> ids = new HashSet<>();

            for (ParsedOrder order : parsedOrders) {
                String orderId;
                do {
                    orderId = OrderIdGenerator.generate();
                } while (!ids.add(orderId));
                records.add(new OrderRecord(orderId, order));
            }

            // 1. FTP 업로드 수행
            String uploadedFile = fileService.upload(records, applicantKey);
            LogWriter.writeLog(scenarioName, "INFO", "FTP 파일 업로드 성공: " + uploadedFile);

            try {
                // 2. DB Batch Insert 수행
                orderRepository.saveAll(records, applicantKey);
                LogWriter.writeLog(scenarioName, "SUCCESS", "주문 DB(ORDER_TB) 적재 완료 (건수: " + records.size() + ")");
            } catch (Exception e) {
                // 3. 실패 시 FTP 보상 삭제 수행
                LogWriter.writeLog(scenarioName, "WARN", "DB 적재 실패로 인한 FTP 보상 파일 삭제 시도: " + uploadedFile);
                try {
                    fileService.delete(uploadedFile);
                    LogWriter.writeLog(scenarioName, "INFO", "FTP 보상 파일 삭제 완료");
                } catch (Exception ignored) {}
                throw e;
            }

        } catch (Exception e) {
            LogWriter.writeLog(scenarioName, "FAIL", "작업 중 예외 발생: " + e.getMessage());
            throw e;
        }
    }
}