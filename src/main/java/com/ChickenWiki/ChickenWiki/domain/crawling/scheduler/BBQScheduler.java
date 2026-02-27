package com.ChickenWiki.ChickenWiki.domain.crawling.scheduler;

import com.ChickenWiki.ChickenWiki.domain.crawling.service.BBQCrawlerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BBQScheduler {

    private final BBQCrawlerService bbqCrawlerService;

    @Scheduled(cron = "0 0 3 * * MON") // 매주 월요일 새벽 3시
    public void scheduleBbqCrawling() throws Exception {
        bbqCrawlerService.crawlBbqMenu();
    }
}