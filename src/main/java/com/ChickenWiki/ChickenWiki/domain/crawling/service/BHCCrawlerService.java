package com.ChickenWiki.ChickenWiki.domain.crawling.service;

import com.ChickenWiki.ChickenWiki.domain.crawling.model.CrawledMenuSnapshot;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BHCCrawlerService {

    private static final String BRAND_NAME = "BHC";
    private static final String CATEGORY_LIST_URL = "https://www.bhc.co.kr/api/v1/web/categories/list";
    private static final String CATEGORY_PRODUCTS_URL_PREFIX = "https://www.bhc.co.kr/api/v1/web/categories/";
    private static final String DETAIL_API_URL_PREFIX = "https://www.bhc.co.kr/api/v1/web/products/";

    private final MenuCrawlSyncService menuCrawlSyncService;
    private final CrawlRequestDelayService crawlRequestDelayService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public void crawlBhcMenu() throws Exception {
        System.out.println("BHC 크롤링 시작...");

        Map<Long, CrawledMenuSnapshot> crawledMenuMap = new LinkedHashMap<>();

        Document categoryDoc = Jsoup.connect(CATEGORY_LIST_URL)
                .ignoreContentType(true)
                .userAgent("Mozilla/5.0")
                .timeout(10000)
                .get();
        crawlRequestDelayService.pause();

        JsonNode categoryRoot = objectMapper.readTree(categoryDoc.body().text());
        JsonNode categoryBody = categoryRoot.get("body");

        if (categoryBody == null || !categoryBody.isArray()) {
            throw new IllegalStateException("BHC 카테고리 응답 body가 배열이 아닙니다.");
        }

        for (JsonNode category : categoryBody) {
            Integer topCateId = getIntValue(category, "cateIdx");
            String topCateName = cleanText(getTextValue(category, "cateNm"));

            if (topCateId == null || topCateName.isBlank()) {
                continue;
            }

            if (!isChickenTopCategory(topCateName)) {
                continue;
            }

            String productsUrl = CATEGORY_PRODUCTS_URL_PREFIX + topCateId + "/products";
            System.out.println("BHC 카테고리 크롤링 중: " + topCateName + " / " + productsUrl);

            Document productsDoc = Jsoup.connect(productsUrl)
                    .ignoreContentType(true)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();
            crawlRequestDelayService.pause();

            JsonNode productsRoot = objectMapper.readTree(productsDoc.body().text());
            JsonNode productsBody = productsRoot.get("body");

            if (productsBody == null || !productsBody.isArray()) {
                continue;
            }

            for (JsonNode item : productsBody) {
                if (!shouldSaveMenu(item, topCateName)) {
                    continue;
                }

                Long sourceMenuId = parseLongSafe(getTextValue(item, "productCd"));
                if (sourceMenuId == null) {
                    continue;
                }

                String menuName = cleanText(getTextValue(item, "productNm"));
                String description = getDescriptionFromList(item);
                String menuImageUrl = cleanText(getTextValue(item, "mainImg"));
                Integer menuPrice = fetchPriceByProductCd(sourceMenuId);

                if (menuName.isBlank()) {
                    continue;
                }

                if (crawledMenuMap.containsKey(sourceMenuId)) {
                    CrawledMenuSnapshot existing = crawledMenuMap.get(sourceMenuId);
                    existing.addOriginalTag(topCateName);
                    addProductOriginalTags(existing, item);
                    addOriginalFlags(existing, item);
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

                data.addOriginalTag(topCateName);
                addProductOriginalTags(data, item);
                addOriginalFlags(data, item);

                crawledMenuMap.put(sourceMenuId, data);
            }
        }

        menuCrawlSyncService.sync(BRAND_NAME, crawledMenuMap.values());
        System.out.println("BHC 크롤링 완료! 저장 건수: " + crawledMenuMap.size());
    }

    private boolean isChickenTopCategory(String topCateName) {
        return "치킨".equals(topCateName)
                || "신메뉴".equals(topCateName)
                || "BHC시그니처".equals(topCateName);
    }

    private boolean shouldSaveMenu(JsonNode item, String topCateName) {
        if (!isChickenTopCategory(topCateName)) {
            return false;
        }

        String menuName = cleanText(getTextValue(item, "productNm"));
        String description = getDescriptionFromList(item);
        String mergedText = (menuName + " " + description).replace("\n", " ");

        if (mergedText.contains("콜팝")) {
            return false;
        }

        if (mergedText.contains("세트")
                || mergedText.contains("[혼치세트]")
                || mergedText.contains("+콜라")
                || mergedText.contains("+치즈볼")
                || mergedText.contains("라이스")
                || mergedText.contains("볶음밥")) {
            return false;
        }

        return true;
    }

    private void addProductOriginalTags(CrawledMenuSnapshot data, JsonNode item) {
        JsonNode cateNmNode = item.get("cateNm");
        if (cateNmNode == null || !cateNmNode.isArray()) {
            return;
        }

        for (JsonNode cateNode : cateNmNode) {
            String tagName = cleanText(cateNode.asText());
            if (!tagName.isBlank()) {
                data.addOriginalTag(tagName);
            }
        }
    }

    private void addOriginalFlags(CrawledMenuSnapshot data, JsonNode item) {
        String isNew = getTextValue(item, "isNew");
        String isBest = getTextValue(item, "isBest");
        String isLimited = getTextValue(item, "isLimited");

        if ("Y".equalsIgnoreCase(isNew)) {
            data.addOriginalTag("신제품");
        }

        if ("Y".equalsIgnoreCase(isBest)) {
            data.addOriginalTag("시그니처");
        }

        if ("Y".equalsIgnoreCase(isLimited)) {
            data.addOriginalTag("기간한정");
        }
    }

    private Integer fetchPriceByProductCd(Long productCd) {
        try {
            String detailUrl = DETAIL_API_URL_PREFIX + productCd;

            Document detailDoc = Jsoup.connect(detailUrl)
                    .ignoreContentType(true)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();
            crawlRequestDelayService.pause();

            JsonNode detailRoot = objectMapper.readTree(detailDoc.body().text());
            JsonNode detailBody = detailRoot.get("body");

            if (detailBody == null || detailBody.isNull()) {
                return 0;
            }

            Integer price = getIntValue(detailBody, "price");
            return price != null ? price : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private String getDescriptionFromList(JsonNode item) {
        String mobileDetailDescription = getTextValue(item, "mobileDetailDescription");
        if (!mobileDetailDescription.isBlank()) {
            return cleanTextPreserveLineBreaks(mobileDetailDescription);
        }

        String mobileListDescription = getTextValue(item, "mobileListDescription");
        if (!mobileListDescription.isBlank()) {
            return cleanTextPreserveLineBreaks(mobileListDescription);
        }

        String description = getTextValue(item, "description");
        if (!description.isBlank()) {
            return cleanTextPreserveLineBreaks(description);
        }

        return "";
    }

    private String getTextValue(JsonNode item, String fieldName) {
        JsonNode node = item.get(fieldName);
        return node != null && !node.isNull() ? node.asText() : "";
    }

    private Integer getIntValue(JsonNode item, String fieldName) {
        JsonNode node = item.get(fieldName);
        return node != null && !node.isNull() ? node.asInt() : 0;
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

    private String cleanText(String text) {
        if (text == null) {
            return "";
        }

        return text.replace("\r", "")
                .replace("\n", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String cleanTextPreserveLineBreaks(String text) {
        if (text == null) {
            return "";
        }

        return text.replace("\r", "")
                .replaceAll("[ \t]+", " ")
                .replaceAll("\n{2,}", "\n")
                .trim();
    }
}
