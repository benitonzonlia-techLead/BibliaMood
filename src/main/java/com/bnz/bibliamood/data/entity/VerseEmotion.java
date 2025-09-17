package com.bnz.bibliamood.data.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "verse_emotion", schema = "bibliamood",
    uniqueConstraints = @UniqueConstraint(name = "uk_verse_emotion_unique", columnNames = {"verse_id", "emotion_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerseEmotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verse_id", nullable = false)
    private Verse verse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emotion_id", nullable = false)
    private Emotion emotion;
    
    @Column(name = "score")
    private Double score;
}
