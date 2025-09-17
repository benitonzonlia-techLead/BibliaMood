package com.bnz.bibliamood.util;

import com.bnz.bibliamood.exception.BibliaMoodException;

import java.util.Map;

public class EmotionConstants {

    private static final String EMOTIONAL_COMPASSION= "COMPASSION";
    private static final String EMOTIONAL_ADMIRATION= "ADMIRATION";
    private static final String EMOTIONAL_SURPRISE= "SURPRISE";

    // Private constructor to prevent instantiation
    private EmotionConstants() {
        throw new BibliaMoodException("Utility class");
    }

    // Mapping labels -> codes internes
    public static final Map<String, String> LABEL_TO_CODE;
    public static final Map<String, String> FRENCH_SENTIMENT_TO_CODE;
    public static final Map<String, String> ENGLISH_SENTIMENT_TO_CODE;
    static {
        LABEL_TO_CODE = Map.ofEntries(
            Map.entry("joy", "JOY"),
            Map.entry("happiness", "JOY"),
            Map.entry("love", "LOVE"),
            Map.entry("trust", "TRUST"),
            Map.entry("hope", "HOPE"),
            Map.entry("gratitude", "GRATITUDE"),
            Map.entry("admiration", EMOTIONAL_ADMIRATION),
            Map.entry("fear", "FEAR"),
            Map.entry("sadness", "SADNESS"),
            Map.entry("anger", "ANGER"),
            Map.entry("disgust", "DISGUST"),
            Map.entry("surprise", EMOTIONAL_SURPRISE),
            Map.entry("guilt", "GUILT"),
            Map.entry("shame", "SHAME"),
            Map.entry("compassion", EMOTIONAL_COMPASSION),
            Map.entry("zeal", "ZEAL"),
            Map.entry("reverence", "REVERENCE"),
            Map.entry("doubt", "DOUBT"),
            Map.entry("loneliness", "LONELINESS"),
            Map.entry("relief", "RELIEF")
        );

        // Le modèle multilingue renvoie typiquement '1 star' ... '5 stars'
        FRENCH_SENTIMENT_TO_CODE = Map.ofEntries(
                Map.entry("1 star", "TRISTESSE"),
                Map.entry("2 stars", "PEUR"),
                Map.entry("3 stars", "DOUTE"),
                Map.entry("4 stars", "PAIX"),
                Map.entry("5 stars", "JOIE"),
                Map.entry("6 stars", "AMOUR"),
                Map.entry("7 stars", "CONFIANCE"),
                Map.entry("8 stars", EMOTIONAL_ADMIRATION),
                Map.entry("9 stars", EMOTIONAL_COMPASSION),
                Map.entry("10 stars", "SOULAGEMENT"),
                Map.entry("11 stars", "COLERE"),
                Map.entry("12 stars", "DEGOUT"),
                Map.entry("13 stars", EMOTIONAL_SURPRISE),
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
                Map.entry("8 stars", EMOTIONAL_ADMIRATION),
                Map.entry("9 stars", EMOTIONAL_COMPASSION),
                Map.entry("10 stars", "RELIEF"),
                Map.entry("11 stars", "ANGER"),
                Map.entry("12 stars", "DISGUST"),
                Map.entry("13 stars", EMOTIONAL_SURPRISE),
                Map.entry("14 stars", "GUILT"),
                Map.entry("15 stars", "SHAME"),
                Map.entry("16 stars", "ZEAL"),
                Map.entry("17 stars", "REVERENCE"),
                Map.entry("18 stars", "LONELINESS"),
                Map.entry("19 stars", "HOPE"),
                Map.entry("20 stars", "GRATITUDE")
        );
    }
}