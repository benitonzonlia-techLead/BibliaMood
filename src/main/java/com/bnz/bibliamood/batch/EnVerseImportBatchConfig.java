package com.bnz.bibliamood.batch;

import com.bnz.bibliamood.data.entity.Verse;
import com.bnz.bibliamood.data.repository.VerseRepository;
import com.bnz.bibliamood.util.BibleMappings;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class EnVerseImportBatchConfig extends BaseVerseImportBatchConfig {

    private final VerseRepository verseRepository;


    @Bean(name = "enVerseItemReader")
    public FlatFileItemReader<Verse> verseItemReader() {
        return createReader("bible_csv/kjv_strongs.csv", createVerseLineMapper());
    }


    @Bean(name = "enVerseItemWriter")
    public RepositoryItemWriter<Verse> verseItemWriter() {
        return createWriter(verseRepository);
    }

    @Bean
    public ItemProcessor<Verse, Verse> enVerseProcessor() {
        return verse -> processVerse(verse, "en", "KJV",
                BibleMappings.getCodeToNameMapping("en"),
                BibleMappings.getCodeToNumberMapping());
    }


    @Bean(name = "enImportVerseStep")
    public Step importVerseStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("enImportVerseStep", jobRepository)
                .<Verse, Verse>chunk(100, transactionManager)
                .reader(verseItemReader())
                .processor(enVerseProcessor())
                .writer(verseItemWriter())
                .build();
    }

    @Bean(name = "enImportVerseJob")
    public Job importVerseJob(JobRepository jobRepository, @Qualifier("enImportVerseStep") Step importVerseStep) {
        return new JobBuilder("enImportVerseJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(importVerseStep)
                .build();
    }
}
