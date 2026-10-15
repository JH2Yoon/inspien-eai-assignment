package com.jhy.inspien.scenario1.service;

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

        // 1. FTP 업로드 먼저 수행
        String uploadedFile = fileService.upload(records, applicantKey);

        try {
            // 2. DB Batch Insert 수행
            orderRepository.saveAll(records, applicantKey);
        } catch (Exception e) {
            // 3. 실패 시 FTP 보상 삭제 수행
            try { fileService.delete(uploadedFile); } catch (Exception ignored) {}
            throw e;
        }
    }
}