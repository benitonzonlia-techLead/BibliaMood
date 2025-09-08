package com.bnz.bibliamood.data.repository;

import com.bnz.bibliamood.data.entity.VerseEmotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerseEmotionRepository extends JpaRepository<VerseEmotion, Long> {
}
