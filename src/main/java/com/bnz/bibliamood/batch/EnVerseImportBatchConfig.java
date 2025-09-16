package com.bnz.bibliamood.batch;

import com.bnz.bibliamood.data.entity.Verse;
import com.bnz.bibliamood.data.repository.VerseRepository;
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
            // Nettoyage du texte : suppression des {…}
            if (verse.getText() != null) {
                verse.setText(verse.getText().replaceAll("\\{[^}]*\\}", "").trim());
            }

            // Tables de correspondance
            Map<String, String> codeToName = Map.<String, String>ofEntries(
                    Map.entry("Gen", "Genesis"),
                    Map.entry("Exo", "Exodus"),
                    Map.entry("Lev", "Leviticus"),
                    Map.entry("Num", "Numbers"),
                    Map.entry("Deu", "Deuteronomy"),
                    Map.entry("Jos", "Joshua"),
                    Map.entry("Jdg", "Judges"),
                    Map.entry("Rut", "Ruth"),
                    Map.entry("1Sa", "1 Samuel"),
                    Map.entry("2Sa", "2 Samuel"),
                    Map.entry("1Ki", "1 Kings"),
                    Map.entry("2Ki", "2 Kings"),
                    Map.entry("1Ch", "1 Chronicles"),
                    Map.entry("2Ch", "2 Chronicles"),
                    Map.entry("Ezr", "Ezra"),
                    Map.entry("Neh", "Nehemiah"),
                    Map.entry("Est", "Esther"),
                    Map.entry("Job", "Job"),
                    Map.entry("Psa", "Psalms"),
                    Map.entry("Pro", "Proverbs"),
                    Map.entry("Ecc", "Ecclesiastes"),
                    Map.entry("Sng", "Song of Solomon"),
                    Map.entry("Isa", "Isaiah"),
                    Map.entry("Jer", "Jeremiah"),
                    Map.entry("Lam", "Lamentations"),
                    Map.entry("Eze", "Ezekiel"),
                    Map.entry("Dan", "Daniel"),
                    Map.entry("Hos", "Hosea"),
                    Map.entry("Joe", "Joel"),
                    Map.entry("Amo", "Amos"),
                    Map.entry("Oba", "Obadiah"),
                    Map.entry("Jon", "Jonah"),
                    Map.entry("Mic", "Micah"),
                    Map.entry("Nah", "Nahum"),
                    Map.entry("Hab", "Habakkuk"),
                    Map.entry("Zep", "Zephaniah"),
                    Map.entry("Hag", "Haggai"),
                    Map.entry("Zec", "Zechariah"),
                    Map.entry("Mal", "Malachi"),
                    Map.entry("Mat", "Matthew"),
                    Map.entry("Mar", "Mark"),
                    Map.entry("Luk", "Luke"),
                    Map.entry("Joh", "John"),
                    Map.entry("Act", "Acts"),
                    Map.entry("Rom", "Romans"),
                    Map.entry("1Co", "1 Corinthians"),
                    Map.entry("2Co", "2 Corinthians"),
                    Map.entry("Gal", "Galatians"),
                    Map.entry("Eph", "Ephesians"),
                    Map.entry("Php", "Philippians"),
                    Map.entry("Col", "Colossians"),
                    Map.entry("1Th", "1 Thessalonians"),
                    Map.entry("2Th", "2 Thessalonians"),
                    Map.entry("1Ti", "1 Timothy"),
                    Map.entry("2Ti", "2 Timothy"),
                    Map.entry("Tit", "Titus"),
                    Map.entry("Phm", "Philemon"),
                    Map.entry("Heb", "Hebrews"),
                    Map.entry("Jam", "James"),
                    Map.entry("1Pe", "1 Peter"),
                    Map.entry("2Pe", "2 Peter"),
                    Map.entry("1Jo", "1 John"),
                    Map.entry("2Jo", "2 John"),
                    Map.entry("3Jo", "3 John"),
                    Map.entry("Jud", "Jude"),
                    Map.entry("Rev", "Revelation")
            );

            Map<String, Integer> codeToNumber = Map.<String, Integer>ofEntries(
                    Map.entry("Gen", 1),
                    Map.entry("Exo", 2),
                    Map.entry("Lev", 3),
                    Map.entry("Num", 4),
                    Map.entry("Deu", 5),
                    Map.entry("Jos", 6),
                    Map.entry("Jdg", 7),
                    Map.entry("Rut", 8),
                    Map.entry("1Sa", 9),
                    Map.entry("2Sa", 10),
                    Map.entry("1Ki", 11),
                    Map.entry("2Ki", 12),
                    Map.entry("1Ch", 13),
                    Map.entry("2Ch", 14),
                    Map.entry("Ezr", 15),
                    Map.entry("Neh", 16),
                    Map.entry("Est", 17),
                    Map.entry("Job", 18),
                    Map.entry("Psa", 19),
                    Map.entry("Pro", 20),
                    Map.entry("Ecc", 21),
                    Map.entry("Sng", 22),
                    Map.entry("Isa", 23),
                    Map.entry("Jer", 24),
                    Map.entry("Lam", 25),
                    Map.entry("Eze", 26),
                    Map.entry("Dan", 27),
                    Map.entry("Hos", 28),
                    Map.entry("Joe", 29),
                    Map.entry("Amo", 30),
                    Map.entry("Oba", 31),
                    Map.entry("Jon", 32),
                    Map.entry("Mic", 33),
                    Map.entry("Nah", 34),
                    Map.entry("Hab", 35),
                    Map.entry("Zep", 36),
                    Map.entry("Hag", 37),
                    Map.entry("Zec", 38),
                    Map.entry("Mal", 39),
                    Map.entry("Mat", 40),
                    Map.entry("Mar", 41),
                    Map.entry("Luk", 42),
                    Map.entry("Joh", 43),
                    Map.entry("Act", 44),
                    Map.entry("Rom", 45),
                    Map.entry("1Co", 46),
                    Map.entry("2Co", 47),
                    Map.entry("Gal", 48),
                    Map.entry("Eph", 49),
                    Map.entry("Php", 50),
                    Map.entry("Col", 51),
                    Map.entry("1Th", 52),
                    Map.entry("2Th", 53),
                    Map.entry("1Ti", 54),
                    Map.entry("2Ti", 55),
                    Map.entry("Tit", 56),
                    Map.entry("Phm", 57),
                    Map.entry("Heb", 58),
                    Map.entry("Jam", 59),
                    Map.entry("1Pe", 60),
                    Map.entry("2Pe", 61),
                    Map.entry("1Jo", 62),
                    Map.entry("2Jo", 63),
                    Map.entry("3Jo", 64),
                    Map.entry("Jud", 65),
                    Map.entry("Rev", 66)
            );

            // Si bookCode est vide mais book est renseigné → on le déduit
            if ((verse.getBookCode() == null || verse.getBookCode().isBlank())
                    && verse.getBook() != null) {
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
