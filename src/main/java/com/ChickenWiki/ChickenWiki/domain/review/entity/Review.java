package com.ChickenWiki.ChickenWiki.domain.review.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "TEST_reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long menuId;
    private String author;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;

    public Review(Long menuId, String author, Integer rating, String content) {
        this.menuId = menuId;
        this.author = author;
        this.rating = rating;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }
}
