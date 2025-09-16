package com.bnz.bibliamood.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
class EmotionAnalyzerServiceTest {

    @Test
    void testMappingEnglishLabel() throws Exception {
        EmotionAnalyzerService service = new EmotionAnalyzerService();
        ReflectionTestUtils.setField(service, "modelsEnabled", false);
        // joy -> JOY
        assertThat(service.mapLabelForTest("joy", "en")).isEqualTo("JOY");
        // unknown -> UNKNOWN UPPER
        assertThat(service.mapLabelForTest("transcendence", "en")).isEqualTo("TRANSCENDENCE");
    }

    @Test
    void testMappingFrenchSentimentStars() throws Exception {
        EmotionAnalyzerService service = new EmotionAnalyzerService();
        ReflectionTestUtils.setField(service, "modelsEnabled", false);
        assertThat(service.mapLabelForTest("1 star", "fr")).isEqualTo("TRISTESSE");
        assertThat(service.mapLabelForTest("5 stars", "fr")).isEqualTo("JOIE");
    }

    @Test
    void testAnalyzeTopFallbackEnglish() {
        EmotionAnalyzerService service = new EmotionAnalyzerService();
        ReflectionTestUtils.setField(service, "modelsEnabled", false);

        String text = "love joy peace hope";
        Map<String, Double> result = service.analyzeTopFallback(text, "en", 3);

        assertThat(result).hasSize(3);
        assertThat(result.keySet()).containsExactlyInAnyOrder("LOVE", "JOY", "HOPE");
        assertThat(result.values()).allMatch(score -> score >= 0.0 && score <= 1.0);
    }

    @Test
    void testAnalyzeTopFallbackFrench() {
        EmotionAnalyzerService service = new EmotionAnalyzerService();
        ReflectionTestUtils.setField(service, "modelsEnabled", false);

        String text = "joie tristesse confiance";
        Map<String, Double> result = service.analyzeTopFallback(text, "fr", 3);

        assertThat(result).hasSize(3);
        assertThat(result.keySet()).containsExactlyInAnyOrder("JOY", "SADNESS", "TRUST");
        assertThat(result.values()).allMatch(score -> score >= 0.0 && score <= 1.0);
    }

    @Test
    void testAnalyzeTopWithThreshold() throws Exception {
        EmotionAnalyzerService service = new EmotionAnalyzerService();
        ReflectionTestUtils.setField(service, "modelsEnabled", false);

        String text = "love joy joy joy";
        Map<String, Double> result = service.analyzeTopFallback(text, "en", 3);

        assertThat(result).containsKey("JOY");
        assertThat(result.get("JOY")).isGreaterThan(0.5); // JOY devrait dominer
    }
}
