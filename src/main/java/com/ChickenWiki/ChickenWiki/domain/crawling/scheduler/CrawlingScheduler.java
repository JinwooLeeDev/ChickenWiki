package com.ChickenWiki.ChickenWiki.domain.crawling.scheduler;

import com.ChickenWiki.ChickenWiki.domain.crawling.service.CrawlOrchestratorService;
import com.ChickenWiki.ChickenWiki.domain.crawling.service.CrawlRunResult;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CrawlingScheduler {

    private final CrawlOrchestratorService crawlOrchestratorService;

    @Scheduled(cron = "0 0 3 * * MON")
    public void scheduleAllCrawling() {
        System.out.println("전체 크롤링 스케줄 시작...");

        runScheduled("bbq");
        runScheduled("bhc");
        runScheduled("kyochon");
        runScheduled("goobne");

        System.out.println("전체 크롤링 스케줄 종료...");
    }

    private void runScheduled(String brandCode) {
        try {
            CrawlRunResult result = crawlOrchestratorService.runScheduled(brandCode);
            String message = result.getMessage();
            if (result.getType() == CrawlRunResult.Type.SKIPPED_RECENTLY && result.getNextAllowedAt() != null) {
                message += " 다음 허용 시각: " + result.getNextAllowedAt();
            }
            System.out.println(message);
        } catch (Exception e) {
            System.out.println(brandCode + " 크롤링 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
