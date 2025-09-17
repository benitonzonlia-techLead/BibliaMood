package com.bnz.bibliamood.util;

import com.bnz.bibliamood.exception.BibliaMoodException;

import java.util.Map;

public class BibleMappings {

    // Private constructor to prevent instantiation
    private BibleMappings() {
        throw new BibliaMoodException("Utility class");
    }

    public static Map<String, Integer> getCodeToNumberMapping() {
        return Map.ofEntries(
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
                Map.entry("Mal", 39)
        );
    }

    public static Map<String, String> getCodeToNameMapping(String language) {
        if ("fr".equalsIgnoreCase(language)) {
            return Map.ofEntries(
                    Map.entry("Gen", "Genèse"),
                    Map.entry("Exo", "Exode"),
                    Map.entry("Lev", "Lévitique"),
                    Map.entry("Num", "Nombres"),
                    Map.entry("Deu", "Deutéronome"),
                    Map.entry("Jos", "Josué"),
                    Map.entry("Jdg", "Juges"),
                    Map.entry("Rut", "Ruth"),
                    Map.entry("1Sa", "1 Samuel"),
                    Map.entry("2Sa", "2 Samuel"),
                    Map.entry("1Ki", "1 Rois"),
                    Map.entry("2Ki", "2 Rois"),
                    Map.entry("1Ch", "1 Chroniques"),
                    Map.entry("2Ch", "2 Chroniques"),
                    Map.entry("Ezr", "Esdras"),
                    Map.entry("Neh", "Néhémie"),
                    Map.entry("Est", "Esther"),
                    Map.entry("Job", "Job"),
                    Map.entry("Psa", "Psaumes"),
                    Map.entry("Pro", "Proverbes"),
                    Map.entry("Ecc", "Ecclésiaste"),
                    Map.entry("Sng", "Cantique des Cantiques"),
                    Map.entry("Isa", "Ésaïe"),
                    Map.entry("Jer", "Jérémie"),
                    Map.entry("Lam", "Lamentations"),
                    Map.entry("Eze", "Ézéchiel"),
                    Map.entry("Dan", "Daniel"),
                    Map.entry("Hos", "Osée"),
                    Map.entry("Joe", "Joël"),
                    Map.entry("Amo", "Amos"),
                    Map.entry("Oba", "Abdias"),
                    Map.entry("Jon", "Jonas"),
                    Map.entry("Mic", "Michée"),
                    Map.entry("Nah", "Nahum"),
                    Map.entry("Hab", "Habacuc"),
                    Map.entry("Zep", "Sophonie"),
                    Map.entry("Hag", "Aggée"),
                    Map.entry("Zec", "Zacharie"),
                    Map.entry("Mal", "Malachie")
            );
        } else {
            return Map.ofEntries(
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
                    Map.entry("Mal", "Malachi")
            );
        }
    }
}