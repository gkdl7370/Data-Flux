package com.flux.Data_Flux.repository; // 새로 만든 폴더 경로에 맞춤

import com.flux.Data_Flux.entity.DataPoint; // 엔티티 위치 임포트
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataPointRepository extends JpaRepository<DataPoint, Long> {
}