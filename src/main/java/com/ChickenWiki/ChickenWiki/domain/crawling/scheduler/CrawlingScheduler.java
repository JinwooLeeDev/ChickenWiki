package com.ChickenWiki.ChickenWiki.domain.crawling.scheduler;

import com.ChickenWiki.ChickenWiki.domain.crawling.service.CrawlOrchestratorService;
import com.ChickenWiki.ChickenWiki.domain.crawling.service.CrawlRunResult;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CrawlingScheduler {

    private final CrawlOrchestratorService crawlOrchestratorService;

    @Scheduled(cron = "0 0 3 * * MON")
    public void scheduleAllCrawling() {
        System.out.println("전체 크롤링 스케줄 시작...");

        List<CrawlRunResult> results = crawlOrchestratorService.runAllScheduled();
        logResults(results);

        System.out.println("전체 크롤링 스케줄 종료...");
    }

    private void logResults(List<CrawlRunResult> results) {
        for (CrawlRunResult result : results) {
            String message = result.getMessage();
            if (result.getType() == CrawlRunResult.Type.SKIPPED_RECENTLY && result.getNextAllowedAt() != null) {
                message += " 다음 허용 시각: " + result.getNextAllowedAt();
            }
            System.out.println(message);
        }
    }
}
