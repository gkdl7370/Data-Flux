package com.flux.batch.config;

import com.flux.Data_Flux.entity.DataPoint;
import com.flux.dto.DataPacket;
import com.flux.batch.reader.ExcelItemReader;
import com.flux.batch.processor.DataDecompositionProcessor;
import com.flux.batch.writer.BulkDataWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;

import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class DataIngestJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    // 빈(Bean) 주입
    private final ExcelItemReader excellItemReader;
    private final DataDecompositionProcessor dataDecompositionProcessor;
    private final BulkDataWriter bulkDataWriter;

    @Bean
    public Job dataIngestJob(Step ingestStep) {
        return new JobBuilder("dataIngestJob", jobRepository)
                .start(ingestStep)
                .build();
    }

    @Bean
    public Step ingestStep() {
        return new StepBuilder("ingestStep", jobRepository)
                .<DataPacket, DataPoint>chunk(100, transactionManager) // [중요] Processor의 제네릭과 일치
                .reader(excellItemReader)
                .processor(dataDecompositionProcessor) // 이제 타입이 일치하여 에러가 사라집니다.
                .writer(bulkDataWriter)
                .faultTolerant()
                .skip(IllegalArgumentException.class)
                .skipLimit(10)
                .retryLimit(3)
                .retry(DeadlockLoserDataAccessException.class)
                .build();
    }
}