package com.ChickenWiki.ChickenWiki.domain.crawling.service;

import java.time.LocalDateTime;

public class CrawlRunResult {

    public enum Type {
        SUCCESS,
        SKIPPED_RECENTLY,
        ALREADY_RUNNING
    }

    private final Type type;
    private final String brandCode;
    private final String brandName;
    private final String message;
    private final LocalDateTime nextAllowedAt;

    private CrawlRunResult(Type type,
                           String brandCode,
                           String brandName,
                           String message,
                           LocalDateTime nextAllowedAt) {
        this.type = type;
        this.brandCode = brandCode;
        this.brandName = brandName;
        this.message = message;
        this.nextAllowedAt = nextAllowedAt;
    }

    public static CrawlRunResult success(String brandCode, String brandName, String message) {
        return new CrawlRunResult(Type.SUCCESS, brandCode, brandName, message, null);
    }

    public static CrawlRunResult skippedRecently(String brandCode,
                                                 String brandName,
                                                 String message,
                                                 LocalDateTime nextAllowedAt) {
        return new CrawlRunResult(Type.SKIPPED_RECENTLY, brandCode, brandName, message, nextAllowedAt);
    }

    public static CrawlRunResult alreadyRunning(String brandCode, String brandName, String message) {
        return new CrawlRunResult(Type.ALREADY_RUNNING, brandCode, brandName, message, null);
    }

    public Type getType() {
        return type;
    }

    public String getBrandCode() {
        return brandCode;
    }

    public String getBrandName() {
        return brandName;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getNextAllowedAt() {
        return nextAllowedAt;
    }
}
