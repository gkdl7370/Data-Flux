package com.flux.batch;

import com.flux.Data_Flux.DataFluxApplication;
import com.flux.batch.config.DataIngestJobConfig;
import com.flux.batch.processor.DataDecompositionProcessor;
import com.flux.batch.reader.ExcelItemReader;
import com.flux.batch.writer.BulkDataWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest

@SpringBootTest(classes = {
        DataFluxApplication.class,
        DataIngestJobConfig.class,
        ExcelItemReader.class,
        DataDecompositionProcessor.class,
        BulkDataWriter.class
})
@ActiveProfiles("test")
public class BatchJobIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private ExcelItemReader excelItemReader;

    @Test
    @DisplayName("엑셀 파일을 읽어 DB에 적재")
    public void batchJobTest() throws Exception {
        // given
        // 파일 경로 설정
        String filePath = new File("src/test/resources/sample.xlsx").getAbsolutePath();
        excelItemReader.setFilePath(filePath);

        // when
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        // (선택) DB 저장 확인
        // assertThat(dataPointRepository.count()).isGreaterThan(0);
    }
}