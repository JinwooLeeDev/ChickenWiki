package com.ChickenWiki.ChickenWiki.domain.brand.controller;

import com.ChickenWiki.ChickenWiki.domain.crawling.service.BBQCrawlerService;
import com.ChickenWiki.ChickenWiki.domain.crawling.service.BHCCrawlerService;
import com.ChickenWiki.ChickenWiki.domain.crawling.service.GoobneCrawlerService;
import com.ChickenWiki.ChickenWiki.domain.crawling.service.KyochonCrawlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crawl")
@RequiredArgsConstructor
public class CrawlingController {

    private final BBQCrawlerService bbqCrawlerService;
    private final BHCCrawlerService bhcCrawlerService;
    private final KyochonCrawlerService kyochonCrawlerService;
    private final GoobneCrawlerService goobneCrawlerService;

    @GetMapping("/bbq")
    public String crawlBbq() throws Exception {
        bbqCrawlerService.crawlBbqMenu();
        return "BBQ 크롤링 완료!";
    }

    @GetMapping("/bhc")
    public String crawlBhc() throws Exception {
        bhcCrawlerService.crawlBhcMenu();
        return "BHC 크롤링 완료!";
    }

    @GetMapping("/kyochon")
    public String crawlKyochon() throws Exception {
        kyochonCrawlerService.crawlKyochonMenu();
        return "교촌치킨 크롤링 완료!";
    }

    @GetMapping("/goobne")
    public String crawlGoobne() throws Exception {
        goobneCrawlerService.crawlGoobneMenu();
        return "굽네치킨 크롤링 완료!";
    }
}