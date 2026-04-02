package com.ChickenWiki.ChickenWiki.domain.crawling.service;

import com.ChickenWiki.ChickenWiki.domain.crawling.model.CrawledMenuSnapshot;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class BBQCrawlerService {

    private static final String BRAND_NAME = "BBQ";

    private final MenuCrawlSyncService menuCrawlSyncService;
    private final CrawlRequestDelayService crawlRequestDelayService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Map<Integer, String> BBQ_CATEGORY_MAP = new LinkedHashMap<>();

    static {
        BBQ_CATEGORY_MAP.put(1, "신메뉴");
        BBQ_CATEGORY_MAP.put(2, "후라이드");
        BBQ_CATEGORY_MAP.put(3, "반반");
        BBQ_CATEGORY_MAP.put(4, "시즈닝");
        BBQ_CATEGORY_MAP.put(5, "양념");
        BBQ_CATEGORY_MAP.put(6, "구이");
        BBQ_CATEGORY_MAP.put(8, "1인분메뉴");
    }

    @Transactional
    public void crawlBbqMenu() throws Exception {
        log.info("BBQ 크롤링 시작");

        Map<Long, CrawledMenuSnapshot> crawledMenuMap = new LinkedHashMap<>();

        for (Map.Entry<Integer, String> entry : BBQ_CATEGORY_MAP.entrySet()) {
            Integer categoryId = entry.getKey();
            String originCategory = entry.getValue();

            String url = "https://www.bbq.co.kr/api/delivery/menu/" + categoryId;
            log.debug("BBQ 카테고리 수집 중: {} ({})", originCategory, url);

            Document doc = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();
            crawlRequestDelayService.pause();

            JsonNode items = objectMapper.readTree(doc.body().text());

            for (JsonNode item : items) {
                if (!shouldSaveMenu(item)) {
                    continue;
                }

                Long sourceMenuId = getLongValue(item, "id");
                if (sourceMenuId == null) {
                    continue;
                }

                String menuName = getTextValue(item, "menuName");
                Integer menuPrice = getIntValue(item, "menuPrice");
                String menuImageUrl = getTextValue(item, "menuImageUrl");
                String description = getTextValue(item, "description");

                if (crawledMenuMap.containsKey(sourceMenuId)) {
                    crawledMenuMap.get(sourceMenuId).addOriginalTag(originCategory);
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
                data.addOriginalTag(originCategory);

                crawledMenuMap.put(sourceMenuId, data);
            }
        }

        menuCrawlSyncService.sync(BRAND_NAME, crawledMenuMap.values());
        log.info("BBQ 크롤링 완료 - 수집 메뉴 수: {}", crawledMenuMap.size());
    }

    private boolean shouldSaveMenu(JsonNode item) {
        String menuType = getTextValue(item, "menuType");
        if ("SIDE".equalsIgnoreCase(menuType)) {
            return false;
        }

        String menuName = getTextValue(item, "menuName");

        if (menuName.contains("세트")) {
            return false;
        }

        if (menuName.contains("치킨")) {
            return true;
        }

        return hasChickenOrigin(item);
    }

    private boolean hasChickenOrigin(JsonNode item) {
        JsonNode originNode = item.get("origin");
        if (originNode == null || !originNode.isArray()) {
            return false;
        }

        for (JsonNode originItem : originNode) {
            String name = getTextValue(originItem, "name");
            if (name.contains("닭고기")) {
                return true;
            }
        }

        return false;
    }

    private String getTextValue(JsonNode item, String fieldName) {
        JsonNode node = item.get(fieldName);
        return node != null && !node.isNull() ? node.asText() : "";
    }

    private Integer getIntValue(JsonNode item, String fieldName) {
        JsonNode node = item.get(fieldName);
        return node != null && !node.isNull() ? node.asInt() : 0;
    }

    private Long getLongValue(JsonNode item, String fieldName) {
        JsonNode node = item.get(fieldName);
        return node != null && !node.isNull() ? node.asLong() : null;
    }
}
