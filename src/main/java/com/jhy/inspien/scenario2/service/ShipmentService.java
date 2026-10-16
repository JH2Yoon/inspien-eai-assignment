package com.jhy.inspien.scenario2.service;

import com.jhy.inspien.common.LogWriter;
import com.jhy.inspien.scenario2.dto.ShipmentRecord;
import com.jhy.inspien.scenario2.repository.ShipmentRepository;

import java.util.List;
import java.util.Properties;

public class ShipmentService {
    private final ShipmentRepository shipmentRepository;

    public ShipmentService(Properties props) {
        this.shipmentRepository = new ShipmentRepository(props);
    }

    public void processUnsentOrders(String applicantKey) throws Exception {
        String scenarioName = "SCENARIO_2";
        LogWriter.writeLog(scenarioName, "START", "미전송 주문 조회 및 운송사 DB 적재 배치를 시작합니다.");

        try {
            // 1. 미전송 주문 조회
            List<ShipmentRecord> records = shipmentRepository.findUnsentOrders(applicantKey);

            if (records.isEmpty()) {
                LogWriter.writeLog(scenarioName, "INFO", "전송할 미전송 주문 데이터가 없습니다.");
                return;
            }

            LogWriter.writeLog(scenarioName, "INFO", "조회된 미전송 주문 건수: " + records.size() + "건");

            // 2. 배송 테이블 적재 및 주문 테이블 상태 'Y' 업데이트 수행
            shipmentRepository.processShipmentBatch(records);

            LogWriter.writeLog(scenarioName, "SUCCESS", "운송사 DB 적재 및 주문 상태 'Y' 업데이트 완료");

        } catch (Exception e) {
            LogWriter.writeLog(scenarioName, "FAIL", "배치 실행 중 예외 발생: " + e.getMessage());
            throw e;
        }
    }
}



//package com.jhy.inspien.scenario2.service;
//
//import com.jhy.inspien.scenario2.dto.ShipmentRecord;
//import com.jhy.inspien.scenario2.repository.ShipmentRepository;
//
//import java.util.List;
//import java.util.Properties;
//
//public class ShipmentService {
//    private final ShipmentRepository shipmentRepository;
//
//    public ShipmentService(Properties props) {
//        this.shipmentRepository = new ShipmentRepository(props);
//    }
//
//    public void processUnsentOrders(String applicantKey) throws Exception {
//        // 1. 미전송 주문 조회
//        List<ShipmentRecord> records = shipmentRepository.findUnsentOrders(applicantKey);
//
//        if (records.isEmpty()) {
//            System.out.println("전송할 미전송 주문 데이터가 없습니다.");
//            return;
//        }
//
//        System.out.println("조회된 미전송 주문 건수: " + records.size() + "건");
//
//        // 2. 배송 테이블 적재 및 주문 테이블 상태 'Y' 업데이트 수행
//        shipmentRepository.processShipmentBatch(records);
//    }
//}