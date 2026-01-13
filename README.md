# Data-Flux: 대용량 데이터 안정성 보장 ETL 파이프라인

![Java](https://img.shields.io/badge/Java-17-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-green) ![Spring Batch](https://img.shields.io/badge/Spring%20Batch-5.1-blue) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue) ![Testcontainers](https://img.shields.io/badge/Testcontainers-1.19.3-red)

## 📌 프로젝트 개요 (Overview)
**Data-Flux**는 다양한 포맷(Excel, CSV)의 로우 데이터를 수집하여 정규화된 스키마로 가공 및 적재하는 **고가용성 배치 애플리케이션**입니다. 
기존의 인메모리 리스트 처리 방식에서 발생할 수 있는 OOM(Out Of Memory) 문제를 해결하기 위해 **Spring Batch 5의 Chunk 지향 처리**를 도입했습니다. 또한, 운영 환경에서의 데이터 무결성을 보장하기 위해 강력한 예외 처리 및 재시도 로직을 구현했습니다.

---

## 📈 핵심 성과 (Key Achievements)

### 1. 데이터 처리 안정성 **99.9%** 달성
- **문제**: 외부 엑셀 데이터의 타입 불일치(문자/숫자 혼용)로 인한 배치 중단 빈번 발생
- **해결**: Apache POI `DataFormatter` 도입 및 방어적 파싱 로직 구현
- **성과**: **파싱 에러율 0%** 달성, 잘못된 데이터 유입 시에도 전체 배치가 멈추지 않도록 **Skip Logic(허용 한도 10건)** 적용

### 2. 대용량 처리 성능 최적화
- **기술**: JPA `saveAll` 대신 **Chunk(100건) 단위 트랜잭션** 관리 및 JDBC Batch Update 유도
- **성과**: 건별 Commit 방식 대비 **I/O 횟수 약 100분의 1로 감소**, 대량 데이터 적재 속도 개선

### 3. 테스트 신뢰도 확보 (Environment Isolation)
- **기술**: `Testcontainers`를 도입하여 운영 환경(PostgreSQL)과 100% 동일한 격리된 테스트 환경 구축
- **성과**: 로컬 H2 DB와 운영 DB 간의 문법/제약조건 차이로 인한 **배포 후 장애 가능성 원천 차단**

- ## 🛠 기술 스택 (Tech Stack)
| Category | Technology & Version | Reason for Selection |
| :--- | :--- | :--- |
| **Language** | Java 17 (LTS) | Record, Text Block 등 최신 문법 활용 및 안정성 |
| **Framework** | Spring Boot 3.2.1 | 최신 Spring 6.1 기반의 효율적인 의존성 관리 |
| **Batch** | Spring Batch 5.1.0 | Chunk 지향 처리, 트랜잭션 매니저 개선 등 최신 기능 활용 |
| **Data Access** | Spring Data JPA | 객체 지향적인 데이터 조작 및 유지보수성 향상 |
| **Utility** | Apache POI 5.2.3 | 대용량 엑셀 스트리밍 및 호환성 확보 |
| **Testing** | JUnit 5, Testcontainers 1.19 | 실제 Docker 컨테이너 기반의 통합 테스트 환경 구축 |

---

## 🏗 시스템 아키텍처 & 로직

Spring Batch의 표준인 `Reader-Processor-Writer` 패턴을 준수하여 유지보수성을 높였습니다.

🔹 Robust Reading (ExcelItemReader)
메모리 최적화: 대용량 파일 처리 시 Row 단위 스트리밍 방식을 사용하여 메모리 사용량을 최소화하고 OOM을 방지합니다.

타입 안정성: DataFormatter를 활용하여 셀 타입(Numeric, String, Formula 등)에 상관없이 안전하게 데이터를 문자열로 추출합니다.

🔹 Fault Tolerance (JobConfig)
Skip Logic: IllegalArgumentException 등 데이터 포맷 에러 발생 시 최대 10건까지 건너뛰고 로그를 기록합니다.

Retry Logic: DB 데드락(DeadlockLoserDataAccessException) 등 일시적 장애 발생 시 최대 3회 재시도하여 배치 실패를 방지합니다.

---

## 🚀 트러블슈팅 (Troubleshooting Log)
Data-Flux를 개발하며 직면한 핵심 문제와 해결 과정입니다.

🔍 이슈 1: 엑셀 데이터 타입 불일치로 인한 배치 중단
   상황: 날짜/숫자 셀을 읽을 때 IllegalStateException 발생

   원인: 외부 데이터의 서식이 일정하지 않아 발생한 런타임 에러

   해결: DataFormatter를 적용하여 모든 셀을 포맷팅된 문자열로 통일 추출하도록 리팩토링

🔍 이슈 2: DB 제약조건 위반에 따른 트랜잭션 롤백
   상황: 필수 값(NOT NULL) 누락 데이터 유입 시 전체 배치 롤백

   원인: DataProcessor 단계에서의 검증 부족

   해결: 필수 필드 검증 로직 추가 및 누락 데이터에 대한 기본값 주입(Default Value Strategy) 정책 수립

🔍 이슈 3: 로컬 테스트와 운영 환경의 SQL 호환성 차이
   상황: H2에서 성공한 테스트가 PostgreSQL 운영 환경에서 문법 오류로 실패

   원인: H2와 PostgreSQL 간의 미묘한 SQL 표준 지원 차이

   해결: Testcontainers를 도입해 Docker 컨테이너 기반의 PostgreSQL 테스트 환경을 구축하여 환경 격리 성공

---

## 🏃‍♂️ How to Run

1. 저장소 복제
git clone [https://github.com/gkdl7370/Data-Flux.git](https://github.com/gkdl7370/Data-Flux.git)

2. 빌드 및 테스트 (Docker 필요)
./mvnw clean package

3. 애플리케이션 실행
java -jar target/Data-Flux-0.0.1-SNAPSHOT.jar
