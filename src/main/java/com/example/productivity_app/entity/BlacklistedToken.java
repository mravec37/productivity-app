package com.example.productivity_app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "blacklisted_tokens")
@Getter
@Setter
@AllArgsConstructor
public class BlacklistedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000, unique = true)
    private String token;

    @Column
    private LocalDateTime blacklistedAt;

    public BlacklistedToken() {}

    public BlacklistedToken(String token) {
        this.token = token;
        this.blacklistedAt = LocalDateTime.now();
    }

}
