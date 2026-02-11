package com.flux.batch.processor;

import com.flux.Data_Flux.entity.DataPoint;
import com.flux.dto.DataPacket;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataDecompositionProcessor implements ItemProcessor<DataPacket, List<DataPoint>> {

    @Override
    public List<DataPoint> process(DataPacket item) throws Exception {
        List<DataPoint> decomposedList = new ArrayList<>();

        // DataPacket의 referenceDate ("YYYYMMDD")를 시각 객체로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate baseDate = LocalDate.parse(item.getReferenceDate(), formatter);

        // payload 리스트의 크기만큼 반복 (보통 24회)
        for (int i = 0; i < item.getPayload().size(); i++) {

            // i번째 수치가 null이면 무시 - 데이터 정합성 방어
            Double value = item.getPayload().get(i);
            if (value == null) continue;

            DataPoint dataPoint = DataPoint.builder()
                    .originId(item.getTargetId())
                    .metricValue(value)
                    .metricType(item.getType())
                    .occurredAt(baseDate.atTime(i, 0)) // i시 0분으로 설정
                    .build();

            decomposedList.add(dataPoint);
        }

        return decomposedList;
    }
}