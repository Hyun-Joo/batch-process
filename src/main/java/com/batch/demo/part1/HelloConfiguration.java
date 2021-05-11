package com.batch.demo.part1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class HelloConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    public HelloConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    /**
     * JobInstance의 생성 기준은 JobParameter 중복 여부에 따라 생성
     * 다른 parameter로 Job이 실행되면 JobInstance가 생성
     * 같은 parameter로 Job이 실행되면 이미 생성된 JobInstance가 실행 
     * JobExecution은 항상 새롭게 생성 
     * ex)
     * 	* 처음 실행 시 date parameter가 1월1일로 실행됐다면 1번 JobInstance 생성
     * 	* 다음 실행 시 date parameter가 1월2일로 실행됐다면 2번 JobInstance 생성
     * 	* 다음 실행 시 date parameter가 1월2일로 실행됐다면 2번 JobInstance 재실행
     * 	=> Job이 재실행 대상이 아닌 경우 에러 발생
     * Job) 항상 새로운 JobInstance가 실행될 수 있도록 RunIdIncrementer 제공
     *  - RunIdIncrementer는 항상 다른 run.id를 parameter로 설정
     */
    @Bean
    public Job helloJob(){
        return jobBuilderFactory.get("helloJob")
                .incrementer(new RunIdIncrementer()) //항상 새로운 JobInstance를 만들도록 해줌 (id를 내부에서 자동 생성)
                .start(this.helloStep())
                .build();
    }

    @Bean
    public Step helloStep(){
        return stepBuilderFactory.get("helloStep")
                .tasklet((contribution, chunkContext) -> {
                    log.info("hello spring batch");
                    return RepeatStatus.FINISHED;
                }).build();
    }

}
