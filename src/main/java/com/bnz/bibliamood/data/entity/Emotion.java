package com.bnz.bibliamood.data.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "emotion", schema = "bibliamood")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Emotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code; // ex: JOY, PEACE, etc.

    @Column(nullable = false, length = 100)
    private String nameFr; // nom français

    @Column(length = 100)
    private String nameEn; // nom anglais

    private String description;

    @Column(length = 20)
    private String color;
}
