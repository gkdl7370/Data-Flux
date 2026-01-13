package com.flux.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataPacket {
    private String targetId;      // 식별 ID
    private String referenceDate; // 기준 날짜 (YYYYMMDD)
    private String type;          // 데이터 종류
    private List<Double> payload; // 가로로 나열된 수치 데이터 리스트 (00시~23시 등)
}