package com.ChickenWiki.ChickenWiki.domain.review.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "review_likes", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "review_id"}))
public class ReviewLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public ReviewLike(Long userId, Long reviewId) {
        this.userId = userId;
        this.reviewId = reviewId;
        this.createdAt = LocalDateTime.now();
    }
}
