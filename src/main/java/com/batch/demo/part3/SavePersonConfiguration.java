package com.batch.demo.part3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
public class SavePersonConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    public SavePersonConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, EntityManagerFactory entityManagerFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.entityManagerFactory = entityManagerFactory;
    }

    @Bean
    public Job savePersonJob() throws Exception {
        return this.jobBuilderFactory.get("savePersonJob")
                .incrementer(new RunIdIncrementer())
                .start(this.savePersonStep(null))
                .build();
    }

    @Bean
    @JobScope
    public Step savePersonStep(@Value("#{jobParameters[allow_duplicate]}") String allowDuplicate) throws Exception {
        return this.stepBuilderFactory.get("savePersonStep")
                .<Person, Person>chunk(10)
                .reader(savePersonItemReader())
                .processor(new DuplicateValidationProcessor<>(Person::getName, Boolean.parseBoolean(allowDuplicate)))  // 항상 이름 중복체크를 거침
                .writer(itemWriter())
                .build();
    }

    private ItemWriter<? super Person> itemWriter() throws Exception {
        //return items -> items.forEach(x -> log.info("이름: {}", x.getName()));
        JpaItemWriter<Person> jpaItemWriter = new JpaItemWriterBuilder<Person>()
                .entityManagerFactory(entityManagerFactory)
                .build();
        ItemWriter<Person> logItemWriter = items -> log.info("person.size = {}", items.size());

        // 두 ItemWriter를 실행하게 해줌 (주의해서 설정해야 함!)
        CompositeItemWriter<Person> itemWriter = new CompositeItemWriterBuilder<Person>()
                .delegates(jpaItemWriter, logItemWriter)
                .build();
        itemWriter.afterPropertiesSet();
        return itemWriter;
    }

    private ItemReader<? extends Person> savePersonItemReader() throws Exception {
        DefaultLineMapper<Person> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("name", "age", "address");

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSet -> {
            // csv파일상 이름(0) / 나이(1) / 거주지(2) 순
            String name = fieldSet.readString(0);
            String age = fieldSet.readString(1);
            String address = fieldSet.readString(2);
            return new Person(name, age, address);
        });

        FlatFileItemReader<Person> itemReader = new FlatFileItemReaderBuilder<Person>()
                .name("savePersonItemReader")
                .encoding("UTF-8")
                .resource(new ClassPathResource("person.csv"))
                .linesToSkip(1)
                .lineMapper(lineMapper)
                .build();
        itemReader.afterPropertiesSet();
        return itemReader;
    }

}
