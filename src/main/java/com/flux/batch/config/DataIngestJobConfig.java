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

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataIngestJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

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
                .<DataPacket, List<DataPoint>>chunk(100, transactionManager)
                .reader(excellItemReader)
                .processor(dataDecompositionProcessor)
                .writer(bulkDataWriter)
                .faultTolerant()
                .skip(IllegalArgumentException.class)
                .skipLimit(10)
                .retryLimit(3)
                .retry(DeadlockLoserDataAccessException.class)
                .build();
    }
}