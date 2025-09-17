package com.bnz.bibliamood.batch;

import com.bnz.bibliamood.data.entity.Verse;
import com.bnz.bibliamood.data.repository.VerseRepository;
import com.bnz.bibliamood.util.BibleMappings;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class EnVerseImportBatchConfig {

    private final VerseRepository verseRepository;


    @Bean(name = "EnVerseItemReader")
    public FlatFileItemReader<Verse> verseItemReader() {
        FlatFileItemReader<Verse> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("bible_csv/kjv_strongs.csv"));
        reader.setLinesToSkip(1);
        reader.setLineMapper(verseLineMapper());
        return reader;
    }

    public LineMapper<Verse> verseLineMapper() {
        return (line, lineNumber) -> {
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setQuote('"')
                    .setIgnoreSurroundingSpaces(true)
                    .setTrim(true)
                    .build();

            CSVRecord record;
            try (CSVParser parser = CSVParser.parse(line, format)) {
                record = parser.iterator().next();
            } catch (Exception e) {
                throw new RuntimeException("Error parsing CSV at line " + lineNumber, e);
            }

            // Validate column count
            if (record.size() < 6) {
                throw new IllegalArgumentException("Invalid CSV format: insufficient columns at line " + lineNumber);
            }

            // Compléter les colonnes manquantes
            List<String> cols = new ArrayList<>();
            record.forEach(cols::add);

            Verse verse = new Verse();
            verse.setBook(cols.get(1));
            verse.setBookNumber(Integer.valueOf(cols.get(2)));
            verse.setChapter(Integer.valueOf(cols.get(3)));
            verse.setVerseNumber(Integer.valueOf(cols.get(4)));
            verse.setText(cols.get(5));
            return verse;
        };
    }


    @Bean(name = "enVerseItemWriter")
    public RepositoryItemWriter<Verse> verseItemWriter() {
        RepositoryItemWriter<Verse> writer = new RepositoryItemWriter<>();
        writer.setRepository(verseRepository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public ItemProcessor<Verse, Verse> enVerseProcessor() {
        return verse -> {
            Map<String, String> codeToName = BibleMappings.getCodeToNameMapping("en");
            Map<String, Integer> codeToNumber = BibleMappings.getCodeToNumberMapping();

            if (verse.getBookCode() != null) {
                verse.setBook(codeToName.get(verse.getBookCode()));
                verse.setBookNumber(codeToNumber.get(verse.getBookCode()));
            } else {
                verse.setBookCode(
                        codeToName.entrySet().stream()
                                .filter(e -> e.getValue().equalsIgnoreCase(verse.getBook()))
                                .map(Map.Entry::getKey)
                                .findFirst()
                                .orElse(null)
                );
            }
            verse.setLanguage("en");
            verse.setVersion("KJV");
            return verse;
        };
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
