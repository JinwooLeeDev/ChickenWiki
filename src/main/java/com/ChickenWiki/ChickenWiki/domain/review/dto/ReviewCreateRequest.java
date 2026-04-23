package com.ChickenWiki.ChickenWiki.domain.review.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewCreateRequest {
    private Integer rating;
    private String content;
}
