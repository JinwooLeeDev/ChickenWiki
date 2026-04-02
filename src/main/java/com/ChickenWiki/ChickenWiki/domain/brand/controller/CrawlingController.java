package com.ChickenWiki.ChickenWiki.domain.brand.controller;

import com.ChickenWiki.ChickenWiki.domain.crawling.service.CrawlOrchestratorService;
import com.ChickenWiki.ChickenWiki.domain.crawling.service.CrawlRunResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/crawl")
@RequiredArgsConstructor
public class CrawlingController {

    private final CrawlOrchestratorService crawlOrchestratorService;

    @Value("${app.crawling.admin-token:}")
    private String adminToken;

    @GetMapping("/all")
    public ResponseEntity<String> crawlAll(@RequestHeader(value = "X-Crawl-Token", required = false) String crawlToken) {
        validateManualAccess(crawlToken);
        List<CrawlRunResult> results = crawlOrchestratorService.runAllManual();
        return ResponseEntity.ok(formatBatchResult("전체 브랜드", results));
    }

    @GetMapping("/bbq")
    public ResponseEntity<String> crawlBbq(@RequestHeader(value = "X-Crawl-Token", required = false) String crawlToken) throws Exception {
        return runManualCrawl("bbq", crawlToken);
    }

    @GetMapping("/bhc")
    public ResponseEntity<String> crawlBhc(@RequestHeader(value = "X-Crawl-Token", required = false) String crawlToken) throws Exception {
        return runManualCrawl("bhc", crawlToken);
    }

    @GetMapping("/kyochon")
    public ResponseEntity<String> crawlKyochon(@RequestHeader(value = "X-Crawl-Token", required = false) String crawlToken) throws Exception {
        return runManualCrawl("kyochon", crawlToken);
    }

    @GetMapping("/goobne")
    public ResponseEntity<String> crawlGoobne(@RequestHeader(value = "X-Crawl-Token", required = false) String crawlToken) throws Exception {
        return runManualCrawl("goobne", crawlToken);
    }

    private ResponseEntity<String> runManualCrawl(String brandCode, String crawlToken) throws Exception {
        validateManualAccess(crawlToken);
        CrawlRunResult result = crawlOrchestratorService.runManual(brandCode);

        return switch (result.getType()) {
            case SUCCESS -> ResponseEntity.ok(result.getMessage());
            case SKIPPED_RECENTLY -> ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(result.getMessage() + " 다음 허용 시각: " + result.getNextAllowedAt());
            case ALREADY_RUNNING -> ResponseEntity.status(HttpStatus.CONFLICT).body(result.getMessage());
            case FAILED -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result.getMessage());
        };
    }

    private String formatBatchResult(String title, List<CrawlRunResult> results) {
        String summary = results.stream()
                .map(result -> "- " + result.getBrandName() + ": " + result.getMessage())
                .collect(Collectors.joining("\n"));
        return title + " 크롤링 결과\n" + summary;
    }

    private void validateManualAccess(String crawlToken) {
        if (adminToken == null || adminToken.isBlank()) {
            return;
        }

        if (!adminToken.equals(crawlToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "유효한 크롤링 토큰이 필요합니다.");
        }
    }
}
