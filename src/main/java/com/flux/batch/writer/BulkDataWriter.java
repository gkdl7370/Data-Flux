package com.flux.batch.writer;

import com.flux.Data_Flux.entity.DataPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BulkDataWriter implements ItemWriter<List<DataPoint>> {

    private final JdbcTemplate jdbcTemplate; // JPA 대신 JDBC 직접 사용

    @Override
    public void write(Chunk<? extends List<DataPoint>> chunk) throws Exception {
        //중첩 리스트 평탄화 (List<List<DataPoint>> -> List<DataPoint>)
        List<DataPoint> allPoints = chunk.getItems().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        if (allPoints.isEmpty()) return;

        // 고속 삽입 SQL (JPA Lifecycle을 타지 않으므로 created_at도 직접 삽입)
        String sql = "INSERT INTO standard_data_points (origin_id, metric_value, metric_type, occurred_at, created_at) " +
                "VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                DataPoint data = allPoints.get(i);

                // 각 파라미터에 데이터 매핑
                ps.setString(1, data.getOriginId());       // origin_id
                ps.setDouble(2, data.getMetricValue());    // metric_value
                ps.setString(3, data.getMetricType());     // metric_type
                ps.setTimestamp(4, Timestamp.valueOf(data.getOccurredAt())); // occurred_at
                ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            }

            @Override
            public int getBatchSize() {
                // 한 번의 트랜잭션으로 보낼 데이터 총 개수
                return allPoints.size();
            }
        });
    }
}