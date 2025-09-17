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
public class FrVerseImportBatchConfig extends BaseVerseImportBatchConfig {

    private final VerseRepository verseRepository;

    @Bean(name = "FrVerseItemReader")
    public FlatFileItemReader<Verse> verseItemReader() {
        return createReader("bible_csv/segond_1910.csv", createVerseLineMapper());
    }

    @Bean
    public ItemProcessor<Verse, Verse> frVerseProcessor() {
        return verse -> processVerse(verse, "fr", "LSG",
                BibleMappings.getCodeToNameMapping("fr"),
                BibleMappings.getCodeToNumberMapping());
    }

    @Bean(name = "frVerseItemWriter")
    public RepositoryItemWriter<Verse> verseItemWriter() {
        return createWriter(verseRepository);
    }

    @Bean(name = "frImportVerseStep")
    public Step importVerseStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    return new StepBuilder("frImportVerseStep", jobRepository)
        .<Verse, Verse>chunk(100, transactionManager)
        .reader(verseItemReader())
        .processor(frVerseProcessor())
        .writer(verseItemWriter())
        .build();
    }

    @Bean(name = "frImportVerseJob")
    public Job importVerseJob(JobRepository jobRepository, @Qualifier("frImportVerseStep") Step importVerseStep) {
        return new JobBuilder("frImportVerseJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(importVerseStep)
                .build();
    }
}
