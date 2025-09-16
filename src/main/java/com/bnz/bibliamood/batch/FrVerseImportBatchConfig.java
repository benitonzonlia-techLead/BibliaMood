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
public class FrVerseImportBatchConfig {

    private final VerseRepository verseRepository;

    @Bean(name = "FrVerseItemReader")
    public FlatFileItemReader<Verse> verseItemReader() {
        FlatFileItemReader<Verse> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("bible_csv/segond_1910.csv"));
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

            // Validation en dehors du try: ne sera pas wrappée
            if (record.size() < 6) {
                throw new IllegalArgumentException("Invalid CSV format: insufficient columns at line " + lineNumber);
            }

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


    @Bean
    public ItemProcessor<Verse, Verse> frVerseProcessor() {
        return verse -> {
            // Mapping complet code -> nom français et numéro
            Map<String, String> codeToName = Map.<String, String>ofEntries(
                Map.entry("Gen", "Genèse"),
                Map.entry("Exo", "Exode"),
                Map.entry("Lev", "Lévitique"),
                Map.entry("Nom", "Nombres"),
                Map.entry("Deu", "Deutéronome"),
                Map.entry("Jos", "Josué"),
                Map.entry("Jug", "Juges"),
                Map.entry("Rut", "Ruth"),
                Map.entry("1Sa", "1 Samuel"),
                Map.entry("2Sa", "2 Samuel"),
                Map.entry("1Ro", "1 Rois"),
                Map.entry("2Ro", "2 Rois"),
                Map.entry("1Ch", "1 Chroniques"),
                Map.entry("2Ch", "2 Chroniques"),
                Map.entry("Esd", "Esdras"),
                Map.entry("Neh", "Néhémie"),
                Map.entry("Est", "Esther"),
                Map.entry("Job", "Job"),
                Map.entry("Psa", "Psaumes"),
                Map.entry("Pro", "Proverbes"),
                Map.entry("Ecc", "Ecclésiaste"),
                Map.entry("Can", "Cantique des Cantiques"),
                Map.entry("Esa", "Ésaïe"),
                Map.entry("Jer", "Jérémie"),
                Map.entry("Lam", "Lamentations"),
                Map.entry("Eze", "Ézéchiel"),
                Map.entry("Dan", "Daniel"),
                Map.entry("Hos", "Osée"),
                Map.entry("Joe", "Joël"),
                Map.entry("Amo", "Amos"),
                Map.entry("Abd", "Abdias"),
                Map.entry("Jon", "Jonas"),
                Map.entry("Mic", "Michée"),
                Map.entry("Nah", "Nahum"),
                Map.entry("Hab", "Habacuc"),
                Map.entry("Zep", "Sophonie"),
                Map.entry("Agg", "Aggée"),
                Map.entry("Zac", "Zacharie"),
                Map.entry("Mal", "Malachie"),
                Map.entry("Mat", "Matthieu"),
                Map.entry("Mar", "Marc"),
                Map.entry("Luc", "Luc"),
                Map.entry("Jea", "Jean"),
                Map.entry("Act", "Actes"),
                Map.entry("Rom", "Romains"),
                Map.entry("1Co", "1 Corinthiens"),
                Map.entry("2Co", "2 Corinthiens"),
                Map.entry("Gal", "Galates"),
                Map.entry("Eph", "Éphésiens"),
                Map.entry("Phi", "Philippiens"),
                Map.entry("Col", "Colossiens"),
                Map.entry("1Th", "1 Thessaloniciens"),
                Map.entry("2Th", "2 Thessaloniciens"),
                Map.entry("1Ti", "1 Timothée"),
                Map.entry("2Ti", "2 Timothée"),
                Map.entry("Tit", "Tite"),
                Map.entry("Phm", "Philémon"),
                Map.entry("Heb", "Hébreux"),
                Map.entry("Jac", "Jacques"),
                Map.entry("1Pi", "1 Pierre"),
                Map.entry("2Pi", "2 Pierre"),
                Map.entry("1Je", "1 Jean"),
                Map.entry("2Je", "2 Jean"),
                Map.entry("3Je", "3 Jean"),
                Map.entry("Jud", "Jude"),
                Map.entry("Apo", "Apocalypse")
            );
            Map<String, Integer> codeToNumber = Map.<String, Integer>ofEntries(
                Map.entry("Gen", 1),
                Map.entry("Exo", 2),
                Map.entry("Lev", 3),
                Map.entry("Nom", 4),
                Map.entry("Deu", 5),
                Map.entry("Jos", 6),
                Map.entry("Jug", 7),
                Map.entry("Rut", 8),
                Map.entry("1Sa", 9),
                Map.entry("2Sa", 10),
                Map.entry("1Ro", 11),
                Map.entry("2Ro", 12),
                Map.entry("1Ch", 13),
                Map.entry("2Ch", 14),
                Map.entry("Esd", 15),
                Map.entry("Neh", 16),
                Map.entry("Est", 17),
                Map.entry("Job", 18),
                Map.entry("Psa", 19),
                Map.entry("Pro", 20),
                Map.entry("Ecc", 21),
                Map.entry("Can", 22),
                Map.entry("Esa", 23),
                Map.entry("Jer", 24),
                Map.entry("Lam", 25),
                Map.entry("Eze", 26),
                Map.entry("Dan", 27),
                Map.entry("Hos", 28),
                Map.entry("Joe", 29),
                Map.entry("Amo", 30),
                Map.entry("Abd", 31),
                Map.entry("Jon", 32),
                Map.entry("Mic", 33),
                Map.entry("Nah", 34),
                Map.entry("Hab", 35),
                Map.entry("Zep", 36),
                Map.entry("Agg", 37),
                Map.entry("Zac", 38),
                Map.entry("Mal", 39),
                Map.entry("Mat", 40),
                Map.entry("Mar", 41),
                Map.entry("Luc", 42),
                Map.entry("Jea", 43),
                Map.entry("Act", 44),
                Map.entry("Rom", 45),
                Map.entry("1Co", 46),
                Map.entry("2Co", 47),
                Map.entry("Gal", 48),
                Map.entry("Eph", 49),
                Map.entry("Phi", 50),
                Map.entry("Col", 51),
                Map.entry("1Th", 52),
                Map.entry("2Th", 53),
                Map.entry("1Ti", 54),
                Map.entry("2Ti", 55),
                Map.entry("Tit", 56),
                Map.entry("Phm", 57),
                Map.entry("Heb", 58),
                Map.entry("Jac", 59),
                Map.entry("1Pi", 60),
                Map.entry("2Pi", 61),
                Map.entry("1Je", 62),
                Map.entry("2Je", 63),
                Map.entry("3Je", 64),
                Map.entry("Jud", 65),
                Map.entry("Apo", 66)
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
            verse.setLanguage("fr");
            verse.setVersion("LSG");
            return verse;
        };
    }

    @Bean(name = "frVerseItemWriter")
    public RepositoryItemWriter<Verse> verseItemWriter() {
        RepositoryItemWriter<Verse> writer = new RepositoryItemWriter<>();
        writer.setRepository(verseRepository);
        writer.setMethodName("save");
        return writer;
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
