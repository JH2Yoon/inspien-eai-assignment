# 🚀 Inspien Java Assessment Project

> **프로젝트 설명**: 인스피엔 자바 백엔드 개발 과제 구현 솔루션 (데이터 복호화, XML 파싱, Oracle DB 연동, FTP 연계, 배치 스케줄러 및 공통 로깅)

---

## 📂 프로젝트 패키지 구조 및 상세 설명

```text
src/main/java/com/jhy/inspien/
 ┣ common/
 ┃  ┣ ConnectionInfoDecryptor.java  # [보안/복호화] 휴대폰 번호 기반 SHA-1 해시 생성 후 AES-128 (AES/ECB/PKCS5Padding) 접속 정보/데이터 복호화
 ┃  ┣ OrderIdGenerator.java         # [ID 생성] 1개 영문 대문자 + 3자리 숫자 형식 (예: A113) 커스텀 고유 ID 생성기
 ┃  ┣ SampleXmlExtractor.java       # [유틸] response.json 내 SAMPLE_DATA에서 EUC-KR 적용 샘플 XML 자동 추출
 ┃  ┣ XmlParser.java                # [XML 파싱] DOM Parser를 활용한 주문 HEADER 및 ITEM 데이터 파싱 및 매핑
 ┃  └ LogWriter.java                # [공통 로깅] 요청 단위 (START, SUCCESS, FAIL, INFO) 이력을 logs/execution-history.log에 실시간 기록
 ┣ scenario1/
 ┃  ┣ dto/                          # [DTO] ParsedOrder, OrderRecord 등 주문 데이터 바인딩 객체
 ┃  ┣ file/
 ┃  │  └ OrderFileService.java      # [FTP 연계] UTF-8 제어 채널 / ISO-8859-1 매핑 파일 전송 및 DB 실패 시 FTP 보상 삭제 (Delete)
 ┃  ┣ repository/
 ┃  │  └ OrderRepository.java       # [DB 연동] ORDER_TB 테이블 주문 데이터 Batch Insert 적재
 ┃  ┣ runner/
 ┃  │  ┣ DecryptConnectionInfo.java # [실행기] 접속 정보 복호화 및 샘플 XML 추출 처리 Runner
 ┃  │  └ OrderExecution.java        # [실행기] 시나리오 1 메인 실행 진입점 (OrderService 구동)
 ┃  └ service/
 ┃     └ OrderService.java          # [시나리오 1 메인] XML 파싱 -> ID 생성 -> FTP 업로드 -> DB 적재 (실패 시 FTP 보상 삭제 트랜잭션)
 ┗ scenario2/
    ┣ dto/                          # [DTO] ShipmentRecord 등 배송 연동 DTO
    ┣ repository/
    │  └ ShipmentRepository.java    # [DB 연동] ORDER_TB 미전송 건(STATUS='N') 조회, SHIPMENT_TB 일괄 적재 및 STATUS='Y' 업데이트 (ORA-00001 중복키 방어)
    ┣ runner/
    │  └ ShipmentExecution.java     # [실행기] 시나리오 2 5분 주기 스케줄러 배치 실행 진입점 (ShipmentService 구동)
    └ service/
       └ ShipmentService.java       # [시나리오 2 스케줄러] 5분 주기 (ScheduledExecutorService) 미전송 주문 자동 동기화 배치 서비스
```
---

## 🛠️ 주요 구현 기능 및 아키텍처

1. **보안 및 데이터 복호화 (`ConnectionInfoDecryptor`)**
   * 휴대폰 번호 기반의 SHA-1 해시 생성 후, 앞 16바이트를 AES 키로 활용하여 `AES/ECB/PKCS5Padding` 방식으로 안전하게 DB/FTP 접속 정보를 복호화합니다.

2. **시나리오 1: 주문 접수, 파싱, DB 적재 및 FTP 전송 (`OrderService` / `OrderExecution`)**
   * **고유 ID 생성**: `OrderIdGenerator`를 통해 1개 영문 대문자 + 3자리 숫자 형식(예: `A113`)의 중복 없는 ID를 생성합니다.
   * **FTP 전송 및 인코딩**: 제어 채널 `UTF-8` 및 바이트 매핑(`ISO-8859-1`)을 적용하여 한글 파일명 및 데이터 깨짐 현상을 방지합니다.
   * **보상 트랜잭션 (Compensation)**: FTP 업로드가 선행된 후, DB(`ORDER_TB`) 적재 과정에서 예외가 발생할 경우 업로드했던 FTP 파일을 안전하게 자동 삭제(`delete`)합니다.

3. **시나리오 2: 5분 주기 배치 및 상태 동기화 (`ShipmentService` / `ShipmentExecution`)**
   * **미전송 주문 조회**: `ORDER_TB`에서 `STATUS = 'N'` (또는 NULL)인 데이터를 조회합니다.
   * **배송사 DB 연동**: 조회된 데이터를 운송사 테이블(`SHIPMENT_TB`)에 적재하고, 주문 테이블의 상태를 `STATUS = 'Y'`로 동기화합니다.
   * **중복 키 방어 (`ORA-00001`)**: 공유 DB 환경에서 발생할 수 있는 키 중복 예외를 안전하게 캐치하고 로그로 안내하여 안정성을 높였습니다.

4. **시나리오 공통: 운영 관점 로깅 및 예외 제어 (`LogWriter`)**
   * **로컬 파일 로그 저장**: 모든 요청 단위의 작업 시작(`START`), 성공(`SUCCESS`), 실패(`FAIL`), 정보(`INFO`) 이력을 프로젝트 루트의 `logs/execution-history.log` 파일에 타임스탬프와 함께 기록합니다.
   * **예외 처리 설계**: JDBC 트랜잭션 롤백 및 FTP 통신 에러에 대한 견고한 방어 로직을 적용했습니다.

---

## ⚙️ 환경 설정 및 실행 방법

1. **환경 변수 및 프로퍼티 설정**
   * 시스템 환경 변수 또는 `application.properties`에 아래 값을 설정합니다.
     * `PARTICIPANT_NAME` (참여자 이름)
     * `APPLICANT_KEY` (지원자 키)
     * DB 및 FTP 접속 정보 (`application.properties` 또는 복호화 연동)

2. **실행 순서**
   1. **복호화 및 설정 준비**: `DecryptConnectionInfo`를 실행하여 접속 정보 복호화 및 샘플 XML을 추출합니다.
   2. **시나리오 1 실행**: `OrderExecution`을 실행하여 주문 XML을 처리하고 FTP 전송 및 `ORDER_TB` 적재를 수행합니다.
   3. **시나리오 2 실행**: `ShipmentExecution`을 실행하여 5분 주기 스케줄러 배치를 통해 미전송 주문을 `SHIPMENT_TB`로 적재하고 상태를 갱신합니다.
