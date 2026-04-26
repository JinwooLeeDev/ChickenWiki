package com.ChickenWiki.ChickenWiki.domain.brand.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MenuDto {
    private Long id;
    private String menuName;
    private Integer menuPrice;
    private String menuImageUrl;
    private String description;
    private String brandName;
    private LocalDateTime crawledAt;
    private Long reviewCount;
    private Double averageRating;
}
