package com.bnz.bibliamood.data.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "app_user", schema = "bibliamood")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(unique = true, length = 255)
    private String email;
}
