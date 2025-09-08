package com.bnz.bibliamood.data.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "verse_emotion", schema = "bibliamood")
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
}
