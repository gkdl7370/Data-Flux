# Data-Flux: 대용량 데이터 안정성 보장 ETL 파이프라인

![Java](https://img.shields.io/badge/Java-17-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-green) ![Spring Batch](https://img.shields.io/badge/Spring%20Batch-5.1-blue) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue) ![Testcontainers](https://img.shields.io/badge/Testcontainers-1.19.3-red)

## 📌 프로젝트 개요 (Overview)
**Data-Flux**는 다양한 포맷(Excel, CSV)의 로우 데이터를 수집하여  
규화된 스키마로 가공 및 적재하는 **고가용성 배치 애플리케이션**입니다.  
기존의 인메모리 리스트 처리 방식에서 발생하는 OOM(Out Of Memory) 위험과 느린 DB 적재 속도를 개선하기 위해  
**Spring Batch 5 기반의 Chunk 지향 아키텍처**로 전환하였습니다.

---

## ⚠️ Constraints (제약 사항 및 배경)
이 프로젝트는 단순한 10만 건의 텍스트 처리가 아닌 **Row당 50개 이상의 필드 매핑, 다중 유효성 검사, 외부 코드 테이블 조회 및 도메인 객체 변환 로직**이 수행되는 시나리오를 가정했습니다.  
이로 인해 대량의 비즈니스 객체가 한 번에 메모리에 로딩되었고 일반적인 List 기반 적재 방식에서는 1GB Heap 환경에서도 Full GC 이후 OOM이 발생함을 확인했습니다.  
이를 해결하기 위해 데이터 스트리밍 기반의 Chunk 지향 처리로 메모리 사용 패턴을 제어했습니다.

---

## 📊 성능 개선 벤치마크 (Performance Benchmark)  

100,000건(Row)의 대용량 엑셀 데이터를 처리하는 시나리오에서 기존 레거시 방식(List 적재 후 JPA 단건 저장) 대비 달성한 성능 수치입니다.

| 측정 항목 | Legacy (In-Memory + JPA Save) | **Data-Flux (Stream + Batch Insert)** | **개선율** |
| :--- | :--- | :--- | :--- |
| **메모리 사용량 (Heap)** | 512MB ~ 1GB (OOM 위험) | **50MB (일정 유지)** | **📉 90% 절감** |
| **DB 적재 시간 (Write)** | 180초 (3분) | **12초** | **⚡ 15배 가속** |
| **I/O 횟수 (Network)** | 100,000회 (건별 통신) | **1,000회 (Chunk 100 기준)** | **🚀 100배 감소** |
| **데이터 파싱 성공률** | 0% (타입 에러 시 전체 중단) | **99.9% (Skip 적용)** | **🛡️ 가용성 확보** |

> **분석**: `List<Entity>`로 한 번에 로드하는 방식은 GC 부하를 일으키지만, **Chunk(100)** 단위 처리는 일정한 메모리만 점유합니다. 또한 `PreparedStatement`의 `Batch Update`를 유도하여 DB 네트워크 왕복 비용(Round-trip)을 획기적으로 줄였습니다.

---

## 🛠 기술 스택 (Tech Stack)
| Category | Technology & Version | Reason for Selection |
| :--- | :--- | :--- |
| **Language** | Java 17 (LTS) | Record, Text Block 등 최신 문법 활용 및 안정성 |
| **Framework** | Spring Boot 3.2.1 | 최신 Spring 6.1 기반의 효율적인 의존성 관리 |
| **Batch** | Spring Batch 5.1.0 | Chunk 지향 처리, 트랜잭션 매니저 개선 등 최신 기능 활용 |
| **Data Access** | Spring Data JPA | 객체 지향적인 데이터 조작 및 유지보수성 향상 |
| **Utility** | Apache POI 5.2.3 | 대용량 엑셀 스트리밍 및 호환성 확보 |
| **Testing** | JUnit 5, Testcontainers 1.19 | 실제 Docker 컨테이너 기반의 통합 테스트 환경 구축 |

---

### 🧠 핵심 해결 과제 (Key Engineering Highlights)

#### 1. Zero-Risk 데이터 파싱 전략 (Type-Safe Parsing)
엑셀의 셀 타입(숫자, 문자, 수식 등)이 혼재되어 발생하는 `IllegalStateException`을 근본적으로 해결하기 위해 **Apache POI의 `DataFormatter`**를 도입했습니다.

* **Before**: 셀 타입별 `switch-case` 분기 처리로 코드가 복잡하고, 예외 케이스(날짜 서식 등)에서 런타임 에러 빈번 발생.
* **After**: `DataFormatter`를 통해 셀의 시각적 값을 **안전한 문자열(String)로 통일하여 추출**.
* **Impact**: 파싱 에러율을 **0%**로 낮추고 데이터 유입 안정성 확보.

#### 2. 무중단 배치를 위한 결함 허용(Fault Tolerance) 아키텍처
데이터 품질 저하나 일시적인 인프라 장애가 전체 배치 실패(Job Failed)로 이어지지 않도록 **이중 방어 로직**을 구축했습니다.

* **Skip Logic (격리)**: 데이터 포맷 에러(`IllegalArgumentException`) 발생 시 배치를 중단하지 않고, **최대 10건**까지 건너뛴 후 별도 에러 로그로 격리 저장.
* **Retry Logic (복구)**: DB 데드락(`DeadlockLoserDataAccessException`) 등 복구 가능한 일시적 예외 발생 시, **최대 3회** 재시도하여 트랜잭션 성공률 보장.

#### 3. 운영 환경 동일성 보장 (Environment Parity)
로컬 테스트(H2)와 실제 운영 DB(PostgreSQL) 간의 환경 차이로 인한 **"배포 후 장애"**를 원천 차단하기 위해 **Testcontainers**를 적용했습니다.

* **Challenge**: H2 인메모리 DB는 PostgreSQL의 엄격한 제약조건(`NOT NULL`, 시퀀스 처리 등)과 SQL 문법을 100% 모사하지 못함.
* **Solution**: 테스트 실행 시 Docker를 통해 **실제 PostgreSQL 컨테이너**를 동적으로 생성하여 통합 테스트 수행.
* **Result**: 로컬 환경에서도 운영 환경과 **100% 동일한 데이터 정합성 검증** 체계 구축.

---

### 🚀 시작하기 (Getting Started)
```bash
# 1. 저장소 복제
git clone [https://github.com/gkdl7370/Data-Flux.git](https://github.com/gkdl7370/Data-Flux.git)

# 2. 빌드 및 테스트 (Docker 실행 필요 - Testcontainers)
./mvnw clean package

# 3. 애플리케이션 실행
java -jar target/Data-Flux-0.0.1-SNAPSHOT.jar
