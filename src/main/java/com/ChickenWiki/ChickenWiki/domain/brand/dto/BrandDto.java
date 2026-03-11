package com.ChickenWiki.ChickenWiki.domain.brand.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BrandDto {
    private Long id;
    private String name;
    private String logoUrl;
}
