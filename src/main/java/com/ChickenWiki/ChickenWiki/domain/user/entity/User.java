package com.ChickenWiki.ChickenWiki.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;
    private String password;
    private String role;
    private LocalDateTime createdAt;

    public User(String nickname, String password, String role) {
        this.nickname = nickname;
        this.password = password;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }
}