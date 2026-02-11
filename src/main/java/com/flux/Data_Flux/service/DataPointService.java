package com.flux.Data_Flux.service;

import com.flux.Data_Flux.entity.DataPoint;
import com.flux.Data_Flux.repository.DataPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DataPointService {
    // 현재는 사용X
    // 후에 데이터 입력 API 만들때 사용할 구조 예시로 작성해놓은거
    private final DataPointRepository dataPointRepository;

    @Transactional
    public Long registerDataPoint(DataPoint dataPoint) {
        // 데이터 유효성 검증 로직 추가 가능 (예: 수치 범위 체크)
        if (dataPoint.getMetricValue() != null && dataPoint.getMetricValue() < 0) {
            throw new IllegalArgumentException("측정 수치는 음수일 수 없습니다.");
        }
        return dataPointRepository.save(dataPoint).getId();
    }
}