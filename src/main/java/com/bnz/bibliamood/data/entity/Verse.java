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


    @Column(name = "book_code", length = 10)
    private String bookCode;

    @Column(length = 100)
    private String book;

    @Column(name = "book_number")
    private Integer bookNumber;

    @Column()
    private Integer chapter;

    @Column(name = "verse_number")
    private Integer verseNumber;

    @Column(length = 2000)
    private String text;

    @Column(length = 10)
    private String language; // 'fr' ou 'en'

    @Column(length = 20)
    private String version; // ex: 'LSG', 'KJV', 'WEB'
}
