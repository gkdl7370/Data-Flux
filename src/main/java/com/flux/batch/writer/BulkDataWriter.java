package com.flux.batch.writer;

import com.flux.Data_Flux.entity.DataPoint;
import com.flux.Data_Flux.repository.DataPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BulkDataWriter implements ItemWriter<DataPoint> {

    private final DataPointRepository dataPointRepository;

    @Override
    public void write(Chunk<? extends DataPoint> chunk) throws Exception {
        // [비즈니스 로직] 읽어온 데이터를 한 번에 저장
        dataPointRepository.saveAll(chunk.getItems());
    }
}