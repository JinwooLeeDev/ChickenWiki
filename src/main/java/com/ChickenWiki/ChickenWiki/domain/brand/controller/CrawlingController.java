package com.ChickenWiki.ChickenWiki.domain.brand.controller;

import com.ChickenWiki.ChickenWiki.domain.crawling.service.BBQCrawlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crawl")
@RequiredArgsConstructor
public class CrawlingController {

    private final BBQCrawlerService bbqCrawlerService;

    @GetMapping("/bbq")
    public String crawlBbq() throws Exception {
        bbqCrawlerService.crawlBbqMenu();
        return "BBQ 크롤링 완료!";
    }
}
