package com.flux.batch.processor;

import com.flux.Data_Flux.entity.DataPoint;
import com.flux.dto.DataPacket;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataDecompositionProcessor implements ItemProcessor<DataPacket, DataPoint> {

    @Override
    public DataPoint process(DataPacket item) throws Exception {
        // [비즈니스 로직] DataPacket -> DataPoint 변환

        // 1. 엑셀에서 읽은 날짜 문자열이 있다면 파싱 (형식이 확실치 않다면 우선 현재 시간 사용)
        // 만약 item.getReferenceDate()가 "yyyy-MM-dd HH:mm:ss" 형식이라면 파싱
        LocalDateTime occurredAt = LocalDateTime.now();

        return DataPoint.builder()
                .originId(item.getTargetId())
                .metricValue(item.getPayload().get(0))
                .metricType(item.getType())
                .occurredAt(occurredAt)
                .build();
    }
}