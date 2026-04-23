package com.ChickenWiki.ChickenWiki.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserReviewSummaryResponse {
    private Long id;
    private Long menuId;
    private String menuName;
    private String brandName;
    private String menuImageUrl;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
}
