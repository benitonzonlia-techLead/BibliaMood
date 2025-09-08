package com.bnz.bibliamood.data.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "verse", schema = "bibliamood")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Verse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String book;

    @Column(nullable = false)
    private Integer chapter;

    @Column(name = "verse_number", nullable = false)
    private Integer verseNumber;

    @Column(nullable = false, length = 2000)
    private String text;

    @Column(nullable = false, length = 10)
    private String language; // 'fr' ou 'en'

    @Column(length = 20)
    private String version; // ex: 'LSG', 'KJV', 'WEB'
}
