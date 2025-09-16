package com.bnz.bibliamood.batch;

import com.bnz.bibliamood.data.entity.Emotion;
import com.bnz.bibliamood.data.entity.Verse;
import com.bnz.bibliamood.data.entity.VerseEmotion;
import com.bnz.bibliamood.data.repository.EmotionRepository;
import com.bnz.bibliamood.data.repository.VerseEmotionRepository;
import com.bnz.bibliamood.data.repository.VerseRepository;
import com.bnz.bibliamood.service.EmotionAnalyzerService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;


@Configuration
@RequiredArgsConstructor
public class VerseEmotionAssociationBatchConfig {

    private final VerseRepository verseRepository;
    private final EmotionRepository emotionRepository;
    private final VerseEmotionRepository verseEmotionRepository;
    private final EmotionAnalyzerService emotionAnalyzerService;

    @Bean
    @StepScope
    public RepositoryItemReader<Verse> verseEmotionItemReader(@Value("#{jobParameters['lang']}") String lang) {
        RepositoryItemReaderBuilder<Verse> builder = new RepositoryItemReaderBuilder<Verse>()
                .name("verseEmotionItemReader")
                .repository(verseRepository)
                .methodName(lang != null && !lang.isBlank() ? "findAllByLanguageOrderByIdAsc" : "findAllByOrderByIdAsc")
                .arguments(lang != null && !lang.isBlank() ? java.util.List.of(lang) : java.util.List.of())
                .pageSize(500)
                .sorts(java.util.Map.of("id", Sort.Direction.ASC));
        return builder.build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Verse, Verse> verseEmotionProcessor(
            @Value("#{jobParameters['topK'] ?: 3}") Integer topK,
            @Value("#{jobParameters['threshold'] ?: 0.35}") Double threshold
    ) {
        // Pass-through; multi-association handled in writer
        return verse -> verse;
    }

    @Bean
    @StepScope
    public ItemWriter<Verse> verseEmotionWriter(
            @Value("#{jobParameters['topK'] ?: 3}") Integer topK,
            @Value("#{jobParameters['threshold'] ?: 0.35}") Double threshold
    ) {
        int k = topK == null ? 3 : topK;
        double th = threshold == null ? 0.35 : threshold;
        return items -> {
            for (Verse verse : items) {
                java.util.Map<String, Double> top;
                try {
                    top = emotionAnalyzerService.analyzeTop(verse.getText(), verse.getLanguage(), k);
                } catch (Exception e) {
                    continue; // skip sur erreur modèle
                }
                java.util.List<VerseEmotion> toSave = new java.util.ArrayList<>();
                for (java.util.Map.Entry<String, Double> e : top.entrySet()) {
                    String code = e.getKey();
                    double score = e.getValue();
                    if (score < th) continue;
                    Emotion emotion = emotionRepository.findByCode(code).orElse(null);
                    if (emotion == null) continue;
                    if (verseEmotionRepository.existsByVerseAndEmotion(verse, emotion)) continue;
                    toSave.add(VerseEmotion.builder().verse(verse).emotion(emotion).score(score).build());
                }
                if (!toSave.isEmpty()) {
                    verseEmotionRepository.saveAll(toSave);
                }
            }
        };
    }

    @Bean
    public Step associateVerseEmotionStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("associateVerseEmotionStep", jobRepository)
                .<Verse, Verse>chunk(200, txManager)
                .reader(verseEmotionItemReader(null))
                .processor(verseEmotionProcessor(null, null))
                .writer(verseEmotionWriter(null, null))
                .build();
    }

    @Bean
    public Job associateVerseEmotionJob(JobRepository jobRepository, Step associateVerseEmotionStep) {
        return new JobBuilder("associateVerseEmotionJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(associateVerseEmotionStep)
                .build();
    }
}
