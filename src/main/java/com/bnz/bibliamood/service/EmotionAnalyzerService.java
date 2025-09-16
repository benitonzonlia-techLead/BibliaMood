package com.bnz.bibliamood.service;

import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmotionAnalyzerService {

    private ZooModel<String, Classifications> englishModel;
    private ZooModel<String, Classifications> frenchModel; // sentiment -> mapping custom


    @Value("${emotion.models.enabled:true}")
    private boolean modelsEnabled;

    // Mapping labels -> codes internes
    private static final Map<String, String> LABEL_TO_CODE;
    private static final Map<String, String> FRENCH_SENTIMENT_TO_CODE; // 1-5 stars mapping
    private static final Map<String, String> ENGLISH_SENTIMENT_TO_CODE;
    static {
        Map<String, String> base = new HashMap<>();
        base.put("joy", "JOY");
        base.put("happiness", "JOY");
        base.put("love", "LOVE");
        base.put("trust", "TRUST");
        base.put("hope", "HOPE");
        base.put("gratitude", "GRATITUDE");
        base.put("admiration", "ADMIRATION");
        base.put("fear", "FEAR");
        base.put("sadness", "SADNESS");
        base.put("anger", "ANGER");
        base.put("disgust", "DISGUST");
        base.put("surprise", "SURPRISE");
        base.put("guilt", "GUILT");
        base.put("shame", "SHAME");
        base.put("compassion", "COMPASSION");
        base.put("zeal", "ZEAL");
        base.put("reverence", "REVERENCE");
        base.put("doubt", "DOUBT");
        base.put("loneliness", "LONELINESS");
        base.put("relief", "RELIEF");
        LABEL_TO_CODE = Collections.unmodifiableMap(base);

        // Le modèle multilingue renvoie typiquement '1 star' ... '5 stars'
        FRENCH_SENTIMENT_TO_CODE = Map.ofEntries(
                Map.entry("1 star", "TRISTESSE"),
                Map.entry("2 stars", "PEUR"),
                Map.entry("3 stars", "DOUTE"),
                Map.entry("4 stars", "PAIX"),
                Map.entry("5 stars", "JOIE"),
                Map.entry("6 stars", "AMOUR"),
                Map.entry("7 stars", "CONFIANCE"),
                Map.entry("8 stars", "ADMIRATION"),
                Map.entry("9 stars", "COMPASSION"),
                Map.entry("10 stars", "SOULAGEMENT"),
                Map.entry("11 stars", "COLERE"),
                Map.entry("12 stars", "DEGOUT"),
                Map.entry("13 stars", "SURPRISE"),
                Map.entry("14 stars", "CULPABILITE"),
                Map.entry("15 stars", "HONTE"),
                Map.entry("16 stars", "ZËLE"),
                Map.entry("17 stars", "CRAINTE DE DIEU"),
                Map.entry("18 stars", "SOLITUDE"),
                Map.entry("19 stars", "ESPERANCE"),
                Map.entry("20 stars", "RECONNAISSANCE")
        );

        ENGLISH_SENTIMENT_TO_CODE = Map.ofEntries(
                Map.entry("1 star", "SADNESS"),
                Map.entry("2 stars", "FEAR"),
                Map.entry("3 stars", "DOUBT"),
                Map.entry("4 stars", "PEACE"),
                Map.entry("5 stars", "JOY"),
                Map.entry("6 stars", "LOVE"),
                Map.entry("7 stars", "TRUST"),
                Map.entry("8 stars", "ADMIRATION"),
                Map.entry("9 stars", "COMPASSION"),
                Map.entry("10 stars", "RELIEF"),
                Map.entry("11 stars", "ANGER"),
                Map.entry("12 stars", "DISGUST"),
                Map.entry("13 stars", "SURPRISE"),
                Map.entry("14 stars", "GUILT"),
                Map.entry("15 stars", "SHAME"),
                Map.entry("16 stars", "ZEAL"),
                Map.entry("17 stars", "REVERENCE"),
                Map.entry("18 stars", "LONELINESS"),
                Map.entry("19 stars", "HOPE"),
                Map.entry("20 stars", "GRATITUDE")
        );
    }

    @PostConstruct
    void init() {
        if (!modelsEnabled) return;
        try {
            englishModel = ModelZoo.loadModel(Criteria.builder()
                    .setTypes(String.class, Classifications.class)
                    .optModelUrls("djl://ai.djl.huggingface.pytorch/j-hartmann/emotion-english-distilroberta-base")
                    .optEngine("PyTorch")
                    .build());

            frenchModel = ModelZoo.loadModel(Criteria.builder()
                    .setTypes(String.class, Classifications.class)
                    .optModelUrls("djl://ai.djl.huggingface.pytorch/nlptown/bert-base-multilingual-uncased-sentiment")
                    .optEngine("PyTorch")
                    .build());

        } catch (IOException | ModelException e) {
            throw new IllegalStateException("Erreur chargement modèles d'émotion", e);
        }
    }

    @PreDestroy
    void close() {
        if (englishModel != null) englishModel.close();
        if (frenchModel != null) frenchModel.close();
    }

    public String analyze(String text, String language) throws TranslateException {
        if (text == null || text.isBlank()) return "DOUBT"; // fallback
        if (!modelsEnabled) {
            // Fallback déterministe pour les tests
            Map<String, Double> top = analyzeTopFallback(text, language, 1);
            return top.entrySet().iterator().next().getKey();
        }
        Classifications classifications = classify(text, language);
        String rawLabel = classifications.best().getClassName();
        return mapToCode(rawLabel, language);
    }

    public Map<String, Double> analyzeTop(String text, String language, int topK) throws TranslateException {
        if (!modelsEnabled) {
            return analyzeTopFallback(text, language, topK);
        }
        Classifications cls = classify(text, language);
        return cls.items().stream()
                .limit(topK)
                .collect(Collectors.toMap(c -> mapToCode(c.getClassName(), language), Classifications.Classification::getProbability));
    }

    private Classifications classify(String text, String language) throws TranslateException {
        ZooModel<String, Classifications> model = selectModel(language);
        try (Predictor<String, Classifications> predictor = model.newPredictor()) {
            return predictor.predict(text);
        }
    }

    private ZooModel<String, Classifications> selectModel(String language) {
        if (!modelsEnabled) {
            throw new IllegalStateException("Models disabled (emotion.models.enabled=false)");
        }
        if ("en".equalsIgnoreCase(language)) return englishModel;
        if ("fr".equalsIgnoreCase(language)) return frenchModel;
        throw new IllegalArgumentException("Unsupported language: " + language);
    }

    private static final Map<String, String> FR_KEYWORDS;
    private static final Map<String, String> EN_KEYWORDS;

    static {
        FR_KEYWORDS = Map.ofEntries(
            Map.entry("joie", "JOY"),
            Map.entry("bonheur", "JOY"),
            Map.entry("amour", "LOVE"),
            Map.entry("confiance", "TRUST"),
            Map.entry("espoir", "HOPE"),
            Map.entry("gratitude", "GRATITUDE"),
            Map.entry("admiration", "ADMIRATION"),
            Map.entry("peur", "FEAR"),
            Map.entry("tristesse", "SADNESS"),
            Map.entry("colère", "ANGER"),
            Map.entry("dégoût", "DISGUST"),
            Map.entry("surprise", "SURPRISE"),
            Map.entry("culpabilité", "GUILT"),
            Map.entry("honte", "SHAME"),
            Map.entry("compassion", "COMPASSION"),
            Map.entry("zèle", "ZEAL"),
            Map.entry("révérence", "REVERENCE"),
            Map.entry("doute", "DOUBT"),
            Map.entry("solitude", "LONELINESS"),
            Map.entry("soulagement", "RELIEF")
        );

        EN_KEYWORDS = LABEL_TO_CODE;
    }

    private String mapToCode(String rawLabel, String language) {
        String key = rawLabel.toLowerCase();
        // Vérifier dans LABEL_TO_CODE en priorité
        if (LABEL_TO_CODE.containsKey(key)) {
            return LABEL_TO_CODE.get(key);
        }
        // Mapper les scores en étoiles pour les langues spécifiques
        if ("fr".equalsIgnoreCase(language)) {
            return FRENCH_SENTIMENT_TO_CODE.getOrDefault(rawLabel, key.toUpperCase());
        }
        if ("en".equalsIgnoreCase(language)) {
            return ENGLISH_SENTIMENT_TO_CODE.getOrDefault(rawLabel, key.toUpperCase());
        }
        // Retourner la clé en majuscules par défaut
        return key.toUpperCase();
    }

    // Exposed for unit tests when models disabled
    public String mapLabelForTest(String rawLabel, String lang) {
        return mapToCode(rawLabel, lang);
    }

    // --- Fallback heuristique pour tests (sans modèles) ---
    public Map<String, Double> analyzeTopFallback(String text, String language, int topK) {
        String lower = text.toLowerCase();
        Map<String, Integer> counts = new HashMap<>();
        Map<String, String> dict = "fr".equalsIgnoreCase(language) ? FR_KEYWORDS : EN_KEYWORDS;
        for (Map.Entry<String, String> e : dict.entrySet()) {
            String kw = e.getKey();
            String code = e.getValue();
            int c = countOccurrences(lower, kw);
            if (c > 0) counts.merge(code, c, Integer::sum);
        }
        if (counts.isEmpty()) {
            // défaut neutre
            counts.put("DOUBT", 1);
        }
        int total = counts.values().stream().mapToInt(i -> i).sum();
        Map<String, Double> scores = counts.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(Math.max(1, topK))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() / (double) total));
        return scores;
    }

    private int countOccurrences(String text, String sub) {
        int idx = 0, count = 0;
        while ((idx = text.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }
}
