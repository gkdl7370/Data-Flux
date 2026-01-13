package com.flux.Data_Flux.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "standard_data_points")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DataPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "origin_id", nullable = false)
    private String originId; // 원천 데이터 식별자 (예: 지점코드)

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt; // 데이터 실제 발생 시각

    @Column(name = "metric_value")
    private Double metricValue; // 측정 수치

    @Column(name = "metric_type")
    private String metricType; // 데이터 성격 (예: RAIN, TEMP, FLOW)

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}