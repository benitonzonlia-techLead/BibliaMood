package com.bnz.bibliamood.batch;

import com.bnz.bibliamood.data.entity.Verse;
import com.bnz.bibliamood.data.repository.VerseRepository;
import com.bnz.bibliamood.exception.BibliaMoodException;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.core.io.ClassPathResource;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class BaseVerseImportBatchConfig {

    protected FlatFileItemReader<Verse> createReader(String resourcePath, LineMapper<Verse> lineMapper) {
        FlatFileItemReader<Verse> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource(resourcePath));
        reader.setLinesToSkip(1);
        reader.setLineMapper(lineMapper);
        return reader;
    }

    protected RepositoryItemWriter<Verse> createWriter(VerseRepository verseRepository) {
        RepositoryItemWriter<Verse> writer = new RepositoryItemWriter<>();
        writer.setRepository(verseRepository);
        writer.setMethodName("save");
        return writer;
    }

    protected LineMapper<Verse> createVerseLineMapper() {
        return (line, lineNumber) -> {
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setQuote('"')
                    .setIgnoreSurroundingSpaces(true)
                    .setTrim(true)
                    .build();

            CSVRecord rec;
            try (CSVParser parser = CSVParser.parse(line, format)) {
                rec = parser.iterator().next();
            } catch (Exception e) {
                throw new BibliaMoodException("Invalid CSV format at line " + lineNumber);
            }

            if (rec.size() < 6) {
                throw new IllegalArgumentException("Invalid CSV format: insufficient columns at line " + lineNumber);
            }

            List<String> cols = new ArrayList<>();
            rec.forEach(cols::add);

            Verse verse = new Verse();
            verse.setBook(cols.get(1));
            verse.setBookNumber(Integer.valueOf(cols.get(2)));
            verse.setChapter(Integer.valueOf(cols.get(3)));
            verse.setVerseNumber(Integer.valueOf(cols.get(4)));
            verse.setText(cols.get(5));
            return verse;
        };
    }

    protected Verse processVerse(Verse verse, String language, String version, Map<String, String> codeToName, Map<String, Integer> codeToNumber) {
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
        verse.setLanguage(language);
        verse.setVersion(version);
        return verse;
    }
}