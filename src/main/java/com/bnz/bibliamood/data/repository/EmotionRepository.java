package com.bnz.bibliamood.data.repository;

import com.bnz.bibliamood.data.entity.Emotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EmotionRepository extends JpaRepository<Emotion, Long> {
	Optional<Emotion> findByCode(String code);
    boolean existsByCode(String code);

    default Optional<Emotion> saveIfAbsent(Emotion emotion) {
        if (!existsByCode(emotion.getCode())) {
            return Optional.of(save(emotion));
        }
        return findByCode(emotion.getCode());
    }
}
