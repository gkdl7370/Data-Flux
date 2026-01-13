package com.flux.Data_Flux;

import com.flux.Data_Flux.entity.DataPoint;
import com.flux.Data_Flux.repository.DataPointRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class DataFluxApplicationTests {

	@Autowired
	private DataPointRepository dataPointRepository;

	@Test
	@Transactional
	void 가상_DB_저장_및_조회_테스트() {
		// [수정] 생성자 보호로 인해 Builder 사용
		DataPoint point = DataPoint.builder()
				.originId("STATION_001")
				.occurredAt(LocalDateTime.now())
				.metricValue(12.5)
				.metricType("RAIN")
				.build();

		DataPoint saved = dataPointRepository.save(point);
		DataPoint found = dataPointRepository.findById(saved.getId()).orElse(null);

		assertThat(found).isNotNull();
		System.out.println(">>> 가상 DB 저장 성공! ID: " + found.getId());
	}
}