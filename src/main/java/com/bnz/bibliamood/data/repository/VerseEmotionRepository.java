package com.bnz.bibliamood.data.repository;

import com.bnz.bibliamood.data.entity.VerseEmotion;
import com.bnz.bibliamood.data.entity.Verse;
import com.bnz.bibliamood.data.entity.Emotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VerseEmotionRepository extends JpaRepository<VerseEmotion, Long> {
	boolean existsByVerseAndEmotion(Verse verse, Emotion emotion);

    @Query("SELECT ve FROM VerseEmotion ve JOIN FETCH ve.verse")
    List<VerseEmotion> findAllWithVerse();
}
