package com.ChickenWiki.ChickenWiki.domain.crawling.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CrawlRequestDelayService {

    private final long requestDelayMillis;

    public CrawlRequestDelayService(@Value("${app.crawling.request-delay-ms:400}") long requestDelayMillis) {
        this.requestDelayMillis = Math.max(0, requestDelayMillis);
    }

    public void pause() {
        if (requestDelayMillis == 0) {
            return;
        }

        try {
            Thread.sleep(requestDelayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("크롤링 대기 중 인터럽트가 발생했습니다.", e);
        }
    }
}
