package com.bnz.bibliamood.batch;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@SpringBatchTest
public class BatchJobConfigTest {
    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job testJob;

    @Test
    void testJobExecution() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncher.run(testJob, jobParameters);
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }
}
