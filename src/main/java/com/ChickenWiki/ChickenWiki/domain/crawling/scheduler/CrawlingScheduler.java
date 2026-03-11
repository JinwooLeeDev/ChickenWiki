package com.ChickenWiki.ChickenWiki.domain.crawling.scheduler;

import com.ChickenWiki.ChickenWiki.domain.crawling.service.BBQCrawlerService;
import com.ChickenWiki.ChickenWiki.domain.crawling.service.BHCCrawlerService;
import com.ChickenWiki.ChickenWiki.domain.crawling.service.KyochonCrawlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CrawlingScheduler {

    private final BBQCrawlerService bbqCrawlerService;
    private final BHCCrawlerService bhcCrawlerService;
    private final KyochonCrawlerService kyochonCrawlerService;

    @Scheduled(cron = "0 0 3 * * MON") // 매주 월요일 새벽 3시
    public void scheduleAllCrawling() {
        System.out.println("전체 크롤링 스케줄 시작...");

        try {
            System.out.println("BBQ 크롤링 시작");
            bbqCrawlerService.crawlBbqMenu();
            System.out.println("BBQ 크롤링 완료");
        } catch (Exception e) {
            System.out.println("BBQ 크롤링 실패: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            System.out.println("BHC 크롤링 시작");
            bhcCrawlerService.crawlBhcMenu();
            System.out.println("BHC 크롤링 완료");
        } catch (Exception e) {
            System.out.println("BHC 크롤링 실패: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            System.out.println("교촌치킨 크롤링 시작");
            kyochonCrawlerService.crawlKyochonMenu();
            System.out.println("교촌치킨 크롤링 완료");
        } catch (Exception e) {
            System.out.println("교촌치킨 크롤링 실패: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("전체 크롤링 스케줄 종료...");
    }
}