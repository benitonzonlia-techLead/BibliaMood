package com.bnz.bibliamood.batch;

import com.bnz.bibliamood.data.entity.Emotion;
import com.bnz.bibliamood.data.entity.Verse;
import com.bnz.bibliamood.data.entity.VerseEmotion;
import com.bnz.bibliamood.data.repository.EmotionRepository;
import com.bnz.bibliamood.data.repository.VerseEmotionRepository;
import com.bnz.bibliamood.data.repository.VerseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
class VerseEmotionAssociationBatchConfigTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job associateVerseEmotionJob;

    @Autowired
    private VerseRepository verseRepository;

    @Autowired
    private EmotionRepository emotionRepository;

    @Autowired
    private VerseEmotionRepository verseEmotionRepository;

    

    @BeforeEach
    void setup() {
        verseEmotionRepository.deleteAll();
        verseRepository.deleteAll();
        emotionRepository.deleteAll();
        // insère quelques émotions minimales utilisées par mapping
    emotionRepository.save(Emotion.builder().code("JOY").nameFr("Joie").nameEn("Joy").color("#FFD700").build());
    emotionRepository.save(Emotion.builder().code("SADNESS").nameFr("Tristesse").nameEn("Sadness").color("#1E90FF").build());
    emotionRepository.save(Emotion.builder().code("ANGER").nameFr("Colère").nameEn("Anger").color("#DC143C").build());
    emotionRepository.save(Emotion.builder().code("FEAR").nameFr("Peur").nameEn("Fear").color("#A9A9A9").build());
    emotionRepository.save(Emotion.builder().code("LOVE").nameFr("Amour").nameEn("Love").color("#FF69B4").build());

        // échantillon de versets FR et EN
        verseRepository.save(Verse.builder().book("Psaumes").bookNumber(19).chapter(23).verseNumber(1).text("L'Éternel est mon berger, je ne manquerai de rien.").language("fr").version("LSG").build());
        verseRepository.save(Verse.builder().book("John").bookNumber(43).chapter(3).verseNumber(16).text("For God so loved the world that he gave his only Son.").language("en").version("KJV").build());
        verseRepository.save(Verse.builder().book("Job").bookNumber(18).chapter(3).verseNumber(3).text("Let the day perish wherein I was born.").language("en").version("KJV").build());

    }

    @Test
    void testAssociationJobAllLanguages() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("ts", System.currentTimeMillis())
                .addString("topK", "3")
                .addString("threshold", "0.0") // en fallback, scores normalisés >= 0
                .toJobParameters();
        JobExecution exec = jobLauncher.run(associateVerseEmotionJob, params);
        assertThat(exec.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
        // Vérifie des scores renseignés et multi-associations possibles
        java.util.List<VerseEmotion> all = verseEmotionRepository.findAll();
        assertThat(all).isNotEmpty();
        for (VerseEmotion ve : all) {
            assertThat(ve.getScore()).isNotNull();
            assertThat(ve.getScore()).isBetween(0.0, 1.0);
        }
        // Avec topK=3 et threshold=0, on doit avoir potentiellement plusieurs emotions par verset
    long distinctVerseCount = all.stream().map(ve -> ve.getVerse().getId()).distinct().count();
    assertThat((long) all.size()).isGreaterThanOrEqualTo(distinctVerseCount); // >= 1 par verset, souvent >1
    }

    @Test
    void testAssociationJobLanguageFilter() throws Exception {
    JobParameters params = new JobParametersBuilder()
        .addLong("ts", System.currentTimeMillis())
        .addString("lang", "en")
        .addString("topK", "3")
        .addString("threshold", "0.0")
        .toJobParameters();
        JobExecution exec = jobLauncher.run(associateVerseEmotionJob, params);
        assertThat(exec.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
        // Toutes les associations doivent pointer vers des versets EN
        for (VerseEmotion ve : verseEmotionRepository.findAllWithVerse()) {
            assertThat(ve.getVerse().getLanguage()).isEqualTo("en");
        }
    }
}
