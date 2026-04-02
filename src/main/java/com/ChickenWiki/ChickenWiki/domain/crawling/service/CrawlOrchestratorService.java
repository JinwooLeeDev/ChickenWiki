package com.ChickenWiki.ChickenWiki.domain.crawling.service;

import com.ChickenWiki.ChickenWiki.domain.brand.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class CrawlOrchestratorService {

    private static final List<String> ALL_BRAND_CODES = List.of("bbq", "bhc", "kyochon", "goobne");

    private final BBQCrawlerService bbqCrawlerService;
    private final BHCCrawlerService bhcCrawlerService;
    private final KyochonCrawlerService kyochonCrawlerService;
    private final GoobneCrawlerService goobneCrawlerService;
    private final MenuRepository menuRepository;

    private final Map<String, ReentrantLock> brandLocks = new ConcurrentHashMap<>();

    @Value("${app.crawling.minimum-interval-days:7}")
    private long minimumIntervalDays;

    public CrawlRunResult runManual(String brandCode) throws Exception {
        return run(brandCode);
    }

    public CrawlRunResult runScheduled(String brandCode) throws Exception {
        return run(brandCode);
    }

    public List<CrawlRunResult> runAllManual() {
        return runAllSafely();
    }

    public List<CrawlRunResult> runAllScheduled() {
        return runAllSafely();
    }

    private List<CrawlRunResult> runAllSafely() {
        ExecutorService executor = Executors.newFixedThreadPool(ALL_BRAND_CODES.size());
        try {
            List<CompletableFuture<CrawlRunResult>> futures = ALL_BRAND_CODES.stream()
                    .map(brandCode -> CompletableFuture.supplyAsync(() -> safeRun(brandCode), executor))
                    .toList();

            return futures.stream()
                    .map(CompletableFuture::join)
                    .toList();
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executor.shutdownNow();
            }
        }
    }

    private CrawlRunResult safeRun(String brandCode) {
        BrandCrawlDefinition definition = BrandCrawlDefinition.from(brandCode);
        try {
            return run(brandCode);
        } catch (Exception e) {
            return CrawlRunResult.failed(
                    definition.brandCode(),
                    definition.brandName(),
                    definition.brandName() + " 크롤링 실패: " + safeMessage(e)
            );
        }
    }

    private CrawlRunResult run(String brandCode) throws Exception {
        BrandCrawlDefinition definition = BrandCrawlDefinition.from(brandCode);
        ReentrantLock lock = brandLocks.computeIfAbsent(definition.brandCode(), key -> new ReentrantLock());

        if (!lock.tryLock()) {
            return CrawlRunResult.alreadyRunning(
                    definition.brandCode(),
                    definition.brandName(),
                    definition.brandName() + " 크롤링이 이미 실행 중입니다."
            );
        }

        try {
            Duration minimumInterval = Duration.ofDays(Math.max(0, minimumIntervalDays));
            Optional<LocalDateTime> lastSuccessfulCrawl = menuRepository.findLatestCrawledAtByBrandName(definition.brandName());

            if (!minimumInterval.isZero()
                    && lastSuccessfulCrawl.isPresent()
                    && lastSuccessfulCrawl.get().plus(minimumInterval).isAfter(LocalDateTime.now())) {
                return CrawlRunResult.skippedRecently(
                        definition.brandCode(),
                        definition.brandName(),
                        definition.brandName() + " 크롤링은 최근에 이미 성공해서 이번 요청은 건너뜁니다.",
                        lastSuccessfulCrawl.get().plus(minimumInterval)
                );
            }

            execute(definition);
            return CrawlRunResult.success(
                    definition.brandCode(),
                    definition.brandName(),
                    definition.brandName() + " 크롤링이 완료되었습니다."
            );
        } finally {
            lock.unlock();
        }
    }

    private void execute(BrandCrawlDefinition definition) throws Exception {
        switch (definition.brandCode()) {
            case "bbq" -> bbqCrawlerService.crawlBbqMenu();
            case "bhc" -> bhcCrawlerService.crawlBhcMenu();
            case "kyochon" -> kyochonCrawlerService.crawlKyochonMenu();
            case "goobne" -> goobneCrawlerService.crawlGoobneMenu();
            default -> throw new IllegalArgumentException("지원하지 않는 브랜드 코드입니다: " + definition.brandCode());
        }
    }

    private String safeMessage(Exception exception) {
        if (exception == null || exception.getMessage() == null || exception.getMessage().isBlank()) {
            return "원인을 확인하지 못했습니다.";
        }
        return exception.getMessage();
    }

    private record BrandCrawlDefinition(String brandCode, String brandName) {
        private static BrandCrawlDefinition from(String rawBrandCode) {
            String brandCode = rawBrandCode == null ? "" : rawBrandCode.trim().toLowerCase(Locale.ROOT);

            return switch (brandCode) {
                case "bbq" -> new BrandCrawlDefinition("bbq", "BBQ");
                case "bhc" -> new BrandCrawlDefinition("bhc", "BHC");
                case "kyochon" -> new BrandCrawlDefinition("kyochon", "교촌치킨");
                case "goobne" -> new BrandCrawlDefinition("goobne", "굽네치킨");
                default -> throw new IllegalArgumentException("지원하지 않는 브랜드 코드입니다: " + rawBrandCode);
            };
        }
    }
}
