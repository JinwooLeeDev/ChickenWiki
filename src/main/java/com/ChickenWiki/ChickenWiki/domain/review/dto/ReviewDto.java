package com.ChickenWiki.ChickenWiki.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ReviewDto {
    private Long id;
    private Long menuId;
    private String author;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
}
