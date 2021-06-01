package com.batch.demo.part3;

import com.batch.demo.TestConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBatchTest    // 해당 어노테이션이 있어야 JobScope 어노테이션이 동작함
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { SavePersonConfiguration.class, TestConfiguration.class})
public class SavePersonConfigurationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private PersonRepository personRepository;

    @After  // 각 메소드 실행 후 해당 repository 지움
    public void tearDown() throws Exception {
        personRepository.deleteAll();
    }

    @Test
    public void test_step(){
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("savePersonStep");

        //then (데이터 검증)
        Assertions.assertThat(jobExecution.getStepExecutions().stream()
                .mapToInt(StepExecution::getWriteCount)
                .sum())
                .isEqualTo(personRepository.count())
                .isEqualTo(4);
    }

    @Test
    public void test_allow_duplicate() throws Exception {
        //given
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("allow_duplicate", "false")
                .toJobParameters();

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        //then (데이터 검증)
        Assertions.assertThat(jobExecution.getStepExecutions().stream()
                                .mapToInt(StepExecution::getWriteCount)
                                .sum())
                                .isEqualTo(personRepository.count())
                                .isEqualTo(4);
    }

    @Test
    public void test_not_allow_duplicate() throws Exception {
        //given
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("allow_duplicate", "true")
                .toJobParameters();

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        //then (데이터 검증)
        Assertions.assertThat(jobExecution.getStepExecutions().stream()
                .mapToInt(StepExecution::getWriteCount)
                .sum())
                .isEqualTo(personRepository.count())
                .isEqualTo(100);
    }

}
