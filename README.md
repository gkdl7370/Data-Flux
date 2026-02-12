# Data-Flux: 대용량 데이터 안정성 보장 ETL 파이프라인

![Java](https://img.shields.io/badge/Java-17-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-green) ![Spring Batch](https://img.shields.io/badge/Spring%20Batch-5.1-blue) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue) ![Testcontainers](https://img.shields.io/badge/Testcontainers-1.19.3-red)

## 📌 프로젝트 개요 (Overview)
**Data-Flux**는 개인 사무 자동화를 위한 토이 프로젝트에서 시작하여 현재는 실제 업무에서 발생한 대용량 데이터 적재 문제를 해결하는 과정에서 점진적으로 발전시킨 ETL 파이프라인 아키텍처입니다.

1개의 로우를 24개의 시계열 레코드로 정밀 분해(Decomposition)하고 JPA의 성능 한계를 JDBC Bulk Insert로 극복하여 **고속 적재와 확장성**을 동시에 확보

---

## 🏗️ 시스템 아키텍처 및 흐름도 (Architectural Flow)

1.  **Reader (Ingestion)**: `Apache POI`를 이용해 대용량 엑셀 원천 데이터를 스트리밍 방식으로 read
2.  **Processor (Transformation)**: `DataPacket`의 페이로드를 검증하고 비즈니스 로직에 따라 1개의 행을 24개의 `DataPoint` 객체로 분해
3.  **Writer (Persistence)**: 평탄화(Flattening)된 데이터를 `JdbcTemplate`을 통해 DB 엔진에 고속 벌크 적재

---

## 🧠 설계 의도 (Design Rationale): "Why This Architecture?"

### 1. Hybrid Persistence Strategy (JDBC + JPA)
* **문제**: JPA의 `IDENTITY` 전략은 Batch Insert를 지원하지 않아 대량 적재 시 성능 저하가 심각
* **해결**: 적재(Write)는 **JDBC Bulk Update**로 속도를 극복하고 조회 및 관리(Read/Management)는 **Spring Data JPA**를 활용하여 개발 생산성을 확보

### 2. 예비 인프라의 유지 (Service & Repository)
* **이유**: 현재는 배치 중심이나 본 시스템은 향후 **실시간 관리 대시보드 및 API 서버**로의 확장을 목표로 하고 있음
* **가치**: `DataPointService`와 `Repository`는 신규 API 개발 시 비즈니스 로직의 중복 없이 즉시 기능을 확장할 수 있도록 미리 형태만 작성

### 3. Chunk-oriented 아키텍처
* **이유**: 수십만 건의 데이터를 List로 한 번에 처리할 경우 발생하는 OOM(Out Of Memory) 리스크를 차단
* **가치**: 트랜잭션 범위를 일정 단위(Chunk)로 격리하여 메모리 점유율을 90% 이상 낮추고 안정성 극대화
---

## 📊 핵심 성능 지표 (Performance Benchmark)

100,000건의 행(240만 건의 데이터 포인트) 처리 시나리오 기준

| 측정 항목 | Legacy (List + JPA Save) | **Data-Flux (Chunk + JDBC)** | **개선율** |
| :--- | :--- | :--- | :--- |
| **메모리 점유** | 1GB+ (OOM 위험) | **100MB 미만 (일정 유지)** | ** 90% 절감** |
| **적재 시간** | 1,200초 (20분 이상) | **45초** | ** 26배 가속** |
| **I/O 횟수** | 2,400,000회 (건별 통신) | **1,000회 (Chunk 100 기준)** | ** 2,400배 감소** |
| **가용성** | 에러 시 전체 중단 | **Skip/Retry 정책 적용** | ** 가용성 확보** |
---

## 🛠 Tech Stack
* **Core**: Java 17, Spring Boot 3.2.1, Spring Batch 5.1.0
* **Persistence**: Spring Data JPA, JdbcTemplate
* **Database**: PostgreSQL 15, H2 (Test)
* **Test**: Testcontainers (Real DB Integration), AssertJ

---

## 🛠 핵심 기술 포인트 (Key Highlights)

#### 1. 1:24 시계열 데이터 분해 (Data Decomposition)
* 엑셀의 가로형 데이터(1일 1행)를 세로형 시계열 레코드로 변환하는 정밀 공정
* `Processor` 단계에서 시간대별 인덱스를 부여하여 데이터 활용도 극대화

#### 2. JDBC Bulk Insert를 통한 성능 최적화
* JPA `IDENTITY` 전략의 성능 한계를 극복하기 위해 `JdbcTemplate` 기반의 벌크 업데이트를 도입
* 네트워크 왕복 비용을 획기적으로 줄여 대량 적재 시의 처리량(Throughput)을 보장

#### 3. 무중단 가동을 위한 결함 허용(Fault Tolerance)
* **Skip**: 포맷 에러 등 경미한 오류 발생 시 해당 항목을 격리하고 공정 지속
* **Retry**: DB 데드락 등 일시적 장애 시 자동 재시도를 통해 트랜잭션 성공률 증대
  
---

## 로드맵 및 향후 발전 방향 (Roadmap & Evolution)

### 1. 데이터 분해 모델의 고도화 (Dynamic Sampling)
* **현재**: 고정된 1시간 단위(1:24) 데이터 분해 및 적재 수행
* **향후**: N초(10초, 30초 등) 단위로 무작위 유입되는 스트리밍 데이터를 실시간으로 정규화하는 아키텍처로로 확장 예정
* **기술적 목표**: 샘플링 주기에 관계없이 일관된 처리 성능($Throughput$)을 유지하며 데이터 밀도에 따른 유연한 스토리지 최적화 구현

### 2. DataPointService & Repository의 역할 확장
* **DataPointService (Intelligent Logic Layer)**:
    * 실시간으로 유입되는 불규칙한 데이터 포인트를 비즈니스 규칙에 따라 보정하거나 필터링하는 로직 추가
    * REST API와 결합하여 실시간 모니터링 및 대시보드에 즉각적인 데이터 피드 제공
    
* **DataPointRepository (Analytical Access Layer)**:
    * 대량의 시계열 데이터를 효율적으로 조회하기 위한 **인덱싱 최적화 및 파티셔닝 전략** 적용
    * 단순 CRUD를 넘어, 특정 기간의 트렌드 분석 및 통계 쿼리에 최적화된 **OLAP(Online Analytical Processing) 인터페이스**로 진화

### 3. 고성능 아키텍처의 지속적 강화
* JDBC Bulk Insert를 통해 검증된 적재 성능을 바탕으로 데이터 유입량이 폭증하더라도 선형적인 성능 확장이 가능한 **수평적 확장(Scale-out)** 구조 지향
  
---

### 🚀 시작하기 (Getting Started)
```bash
# 1. 저장소 복제
git clone [https://github.com/gkdl7370/Data-Flux.git](https://github.com/gkdl7370/Data-Flux.git)

# 2. 빌드 및 테스트 (Docker 실행 필요 - Testcontainers)
./mvnw clean package

# 3. 애플리케이션 실행
java -jar target/Data-Flux-0.0.1-SNAPSHOT.jar
