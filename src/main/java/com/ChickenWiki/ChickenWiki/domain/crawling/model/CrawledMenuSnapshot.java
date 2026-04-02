package com.ChickenWiki.ChickenWiki.domain.crawling.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class CrawledMenuSnapshot {

    private final Long sourceMenuId;
    private final String menuName;
    private final Integer menuPrice;
    private final String menuImageUrl;
    private final String description;
    private final String brandName;
    private final LinkedHashSet<String> originalTags = new LinkedHashSet<>();

    public CrawledMenuSnapshot(Long sourceMenuId,
                               String menuName,
                               Integer menuPrice,
                               String menuImageUrl,
                               String description,
                               String brandName) {
        this.sourceMenuId = sourceMenuId;
        this.menuName = menuName;
        this.menuPrice = menuPrice;
        this.menuImageUrl = menuImageUrl;
        this.description = description;
        this.brandName = brandName;
    }

    public void addOriginalTag(String tagName) {
        if (tagName != null && !tagName.isBlank()) {
            this.originalTags.add(tagName);
        }
    }

    public Long getSourceMenuId() {
        return sourceMenuId;
    }

    public String getMenuName() {
        return menuName;
    }

    public Integer getMenuPrice() {
        return menuPrice;
    }

    public String getMenuImageUrl() {
        return menuImageUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getBrandName() {
        return brandName;
    }

    public Set<String> getOriginalTags() {
        return originalTags;
    }
}
