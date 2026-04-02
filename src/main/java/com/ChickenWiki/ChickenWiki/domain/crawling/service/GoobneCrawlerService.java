package com.ChickenWiki.ChickenWiki.domain.crawling.service;

import com.ChickenWiki.ChickenWiki.domain.crawling.model.CrawledMenuSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoobneCrawlerService {

    private static final String BRAND_NAME = "굽네치킨";
    private static final String BASE_URL = "https://www.goobne.co.kr";
    private static final String LIST_URL = BASE_URL + "/menu/menu_list_p";
    private static final String DETAIL_URL = BASE_URL + "/menu/menu_view_p";

    private static final CategoryPair[] CATEGORY_PAIRS = {
            new CategoryPair("1", "2", "치킨", "오븐류"),
            new CategoryPair("1", "7", "치킨", "양념류"),
            new CategoryPair("1", "27", "치킨", "기타류"),
            new CategoryPair("3", "4", "신제품", "치킨")
    };

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/144.0.0.0 Safari/537.36";

    private final MenuCrawlSyncService menuCrawlSyncService;
    private final CrawlRequestDelayService crawlRequestDelayService;

    @Transactional
    public void crawlGoobneMenu() throws Exception {
        log.info("굽네치킨 크롤링 시작");

        Map<Long, CrawledMenuSnapshot> crawledMenuMap = new LinkedHashMap<>();

        for (CategoryPair pair : CATEGORY_PAIRS) {
            String url = LIST_URL
                    + "?classId=" + encode(pair.classId)
                    + "&classId2=" + encode(pair.classId2)
                    + "&itemId="
                    + "&source="
                    + "&quickBrId="
                    + "&quickDeliverySeq="
                    + "&dlvType=";

            log.debug("굽네 목록 수집 중: {} > {} ({})", pair.mainCategoryName, pair.subCategoryName, url);

            Document listDoc;
            try {
                listDoc = createHtmlConnection(url, LIST_URL).get();
                crawlRequestDelayService.pause();
            } catch (Exception e) {
                log.warn("굽네 목록 요청 실패 - url={}, error={}", url, e.getMessage());
                continue;
            }

            Elements items = listDoc.select(".menu-grid a.item");
            log.debug("굽네 목록 개수: {}", items.size());

            for (Element item : items) {
                String itemIdText = extractItemId(item.attr("onclick"));
                Long sourceMenuId = parseLongSafe(itemIdText);

                String menuName = extractMenuName(item);
                Integer menuPrice = extractMenuPrice(item);
                String menuImageUrl = extractMenuImageUrl(item);
                String description = "";

                if (sourceMenuId == null || menuName.isBlank()) {
                    log.debug("굽네 메뉴 건너뜀 - itemId 또는 menuName 비어 있음");
                    continue;
                }

                try {
                    String detailUrl = DETAIL_URL + "?itemId=" + encode(itemIdText);
                    Document detailDoc = createHtmlConnection(detailUrl, url).get();
                    crawlRequestDelayService.pause();

                    String detailName = extractDetailMenuName(detailDoc);
                    Integer detailPrice = extractDetailPrice(detailDoc);
                    String detailImageUrl = extractDetailImageUrl(detailDoc);
                    String detailDescription = extractDescription(detailDoc);

                    if (!detailName.isBlank()) {
                        menuName = detailName;
                    }
                    if (detailPrice > 0) {
                        menuPrice = detailPrice;
                    }
                    if (!detailImageUrl.isBlank()) {
                        menuImageUrl = detailImageUrl;
                    }
                    if (!detailDescription.isBlank()) {
                        description = detailDescription;
                    }
                } catch (Exception e) {
                    log.warn("굽네 상세 요청 실패 - itemId={}, error={}", itemIdText, e.getMessage());
                }

                menuName = cleanText(menuName);
                menuImageUrl = cleanText(menuImageUrl);
                description = cleanDescription(description);

                if (!shouldSaveMenu(menuName, description)) {
                    log.debug("굽네 메뉴 필터링 - itemId={}, menuName={}", itemIdText, menuName);
                    continue;
                }

                if (crawledMenuMap.containsKey(sourceMenuId)) {
                    CrawledMenuSnapshot existing = crawledMenuMap.get(sourceMenuId);
                    existing.addOriginalTag(pair.mainCategoryName);
                    existing.addOriginalTag(pair.subCategoryName);
                    continue;
                }

                CrawledMenuSnapshot data = new CrawledMenuSnapshot(
                        sourceMenuId,
                        menuName,
                        menuPrice,
                        menuImageUrl,
                        description,
                        BRAND_NAME
                );
                data.addOriginalTag(pair.mainCategoryName);
                data.addOriginalTag(pair.subCategoryName);

                crawledMenuMap.put(sourceMenuId, data);
            }
        }

        menuCrawlSyncService.sync(BRAND_NAME, crawledMenuMap.values());
        log.info("굽네치킨 크롤링 완료 - 수집 메뉴 수: {}", crawledMenuMap.size());
    }

    private boolean shouldSaveMenu(String menuName, String description) {
        String merged = (menuName + " " + description).replace("\n", " ");

        if (merged.contains("세트") || merged.contains("SET")) {
            return false;
        }

        return true;
    }

    private Connection createHtmlConnection(String url, String referer) {
        return Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("Cache-Control", "no-cache")
                .header("Pragma", "no-cache")
                .header("Referer", referer)
                .header("Origin", BASE_URL)
                .timeout(20000);
    }

    private String extractItemId(String onclick) {
        if (onclick == null || onclick.isBlank()) {
            return "";
        }

        int firstQuote = onclick.indexOf("\"");
        if (firstQuote == -1) return "";

        int secondQuote = onclick.indexOf("\"", firstQuote + 1);
        if (secondQuote == -1) return "";

        return onclick.substring(firstQuote + 1, secondQuote).trim();
    }

    private String extractMenuName(Element item) {
        Element nameElement = item.selectFirst(".textbox h4");
        return nameElement != null ? nameElement.text() : "";
    }

    private Integer extractMenuPrice(Element item) {
        Element priceElement = item.selectFirst(".textbox .price b");
        return parsePrice(priceElement != null ? priceElement.text() : "");
    }

    private String extractMenuImageUrl(Element item) {
        Element imageElement = item.selectFirst("span img");
        return imageElement != null ? imageElement.attr("src") : "";
    }

    private String extractDescription(Document detailDoc) {
        Element descriptionElement = detailDoc.selectFirst(".textArea");
        return descriptionElement != null ? descriptionElement.text() : "";
    }

    private String extractDetailMenuName(Document detailDoc) {
        Element nameElement = detailDoc.selectFirst(".title-flex h2.fs26");
        return nameElement != null ? nameElement.text() : "";
    }

    private Integer extractDetailPrice(Document detailDoc) {
        Element priceElement = detailDoc.selectFirst(".right.textbox .price");
        return parsePrice(priceElement != null ? priceElement.text() : "");
    }

    private String extractDetailImageUrl(Document detailDoc) {
        Element imageElement = detailDoc.selectFirst(".imgArea img");
        return imageElement != null ? imageElement.attr("src") : "";
    }

    private Integer parsePrice(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }

        String numbers = text.replaceAll("[^0-9]", "");
        if (numbers.isBlank()) {
            return 0;
        }

        try {
            return Integer.parseInt(numbers);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String cleanText(String text) {
        if (text == null) {
            return "";
        }

        return text.replace("\r", "")
                .replace("\n", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String cleanDescription(String text) {
        if (text == null) {
            return "";
        }

        return text.replace("\r", "")
                .replaceAll("[ \t]+", " ")
                .replaceAll("\n{2,}", "\n")
                .trim();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private Long parseLongSafe(String value) {
        try {
            if (value == null || value.isBlank()) {
                return null;
            }
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static class CategoryPair {
        private final String classId;
        private final String classId2;
        private final String mainCategoryName;
        private final String subCategoryName;

        public CategoryPair(String classId, String classId2, String mainCategoryName, String subCategoryName) {
            this.classId = classId;
            this.classId2 = classId2;
            this.mainCategoryName = mainCategoryName;
            this.subCategoryName = subCategoryName;
        }
    }
}
