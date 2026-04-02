package com.ChickenWiki.ChickenWiki.domain.review.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "review_likes", uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "reviewId"}))
public class ReviewLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long reviewId;
    private LocalDateTime createdAt;

    public ReviewLike(Long userId, Long reviewId) {
        this.userId = userId;
        this.reviewId = reviewId;
        this.createdAt = LocalDateTime.now();
    }
}