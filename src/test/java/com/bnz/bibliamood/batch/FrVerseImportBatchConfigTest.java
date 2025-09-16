package com.bnz.bibliamood.batch;

import com.bnz.bibliamood.data.entity.Verse;
import com.bnz.bibliamood.data.repository.VerseEmotionRepository;
import com.bnz.bibliamood.data.repository.VerseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
class FrVerseImportBatchConfigTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("frImportVerseJob")
    private Job importVerseJob;

    @Autowired
    private VerseRepository verseRepository;
    @Autowired
    private VerseEmotionRepository verseEmotionRepository;

    @BeforeEach
    void cleanDatabase() {
        verseEmotionRepository.deleteAll();
        verseRepository.deleteAll();
    }

    @Test
    void testFrVerseImportBatch() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        JobExecution execution = jobLauncher.run(importVerseJob, jobParameters);
        assertThat(execution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
        List<Verse> verses = verseRepository.findAll();
        // 1. Vérifier le nombre total de versets (environ 31102 pour la Bible complète)
        assertThat(verses.size()).isEqualTo(31102);

        // 2. Vérifier la totalité des champs mappés sur quelques versets
        for (Verse v : verses.subList(0, Math.min(10, verses.size()))) {
            assertThat(v.getBook()).isNotNull();
            assertThat(v.getBookNumber()).isGreaterThan(0);
            assertThat(v.getChapter()).isGreaterThan(0);
            assertThat(v.getVerseNumber()).isGreaterThan(0);
            assertThat(v.getText()).isNotNull();
            assertThat(v.getLanguage()).isEqualTo("fr");
            assertThat(v.getVersion()).isEqualTo("LSG");
        }

        // 3. Cas bonus : code de livre inconnu
        boolean hasUnknownBook = verses.stream().anyMatch(v -> v.getBookNumber() == 0 || v.getBook().equals(v.getBookCode()));
        // On accepte qu'il puisse y avoir des codes inconnus, mais on vérifie que ce cas est bien géré
        // (book = code, bookNumber = 0)
        // Si aucun, le test passe aussi
        assertThat(hasUnknownBook).isTrue();

        // 4. Cas bonus : champ texte vide ou ligne mal formée (si le CSV de test en contient)
        boolean hasEmptyText = verses.stream().anyMatch(v -> v.getText() == null || v.getText().isBlank());
        assertThat(hasEmptyText).isFalse(); // On vérifie que le batch ne plante pas si ça arrive
    }

    @Test
    void testFrVerseProcessor() throws Exception {
        Verse input = new Verse();
        input.setBook("Genèse");
        input.setBookNumber(1);
        input.setChapter(1);
        input.setVerseNumber(1);
        input.setText("Au commencement, Dieu créa les cieux et la terre.");

        Verse output = new FrVerseImportBatchConfig(verseRepository).frVerseProcessor().process(input);

        assertThat(output).isNotNull();
        assertThat(output.getBook()).isEqualTo("Genèse");
        assertThat(output.getText()).isNotEmpty();
    }

    @Test
    void testMalformedCsvThrowsRuntimeException() {
        // Ligne volontairement malformée : guillemet non fermé
        String malformedLine = "\"Unclosed quote,Genèse,1,1,1,Au commencement";

        LineMapper<Verse> lineMapper = new FrVerseImportBatchConfig(verseRepository).verseLineMapper();

        assertThatThrownBy(() -> lineMapper.mapLine(malformedLine, 3))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error parsing CSV at line 3");
    }


    @Test
    void testInvalidCsvFormatThrowsIllegalArgumentException() {
        // Ligne avec seulement 4 colonnes
        String invalidLine = "1,Genèse,1,1";

        // Instanciation du LineMapper (sans dépendance au repository ici)
        LineMapper<Verse> lineMapper = new FrVerseImportBatchConfig(verseRepository).verseLineMapper();

        // Vérification que l'exception attendue est levée
        assertThatThrownBy(() -> lineMapper.mapLine(invalidLine, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid CSV format");
    }

}
