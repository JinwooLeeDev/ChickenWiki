package com.ChickenWiki.ChickenWiki.domain.crawling.service;

import com.ChickenWiki.ChickenWiki.domain.brand.entity.Menu;
import com.ChickenWiki.ChickenWiki.domain.brand.entity.MenuTagMapping;
import com.ChickenWiki.ChickenWiki.domain.brand.entity.Tag;
import com.ChickenWiki.ChickenWiki.domain.brand.entity.TagType;
import com.ChickenWiki.ChickenWiki.domain.brand.repository.MenuRepository;
import com.ChickenWiki.ChickenWiki.domain.brand.repository.MenuTagMappingRepository;
import com.ChickenWiki.ChickenWiki.domain.brand.repository.TagRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BHCCrawlerService {

    private static final String BRAND_NAME = "BHC";
    private static final String CATEGORY_LIST_URL = "https://www.bhc.co.kr/api/v1/web/categories/list";
    private static final String CATEGORY_PRODUCTS_URL_PREFIX = "https://www.bhc.co.kr/api/v1/web/categories/";
    private static final String DETAIL_API_URL_PREFIX = "https://www.bhc.co.kr/api/v1/web/products/";

    private final MenuRepository menuRepository;
    private final TagRepository tagRepository;
    private final MenuTagMappingRepository menuTagMappingRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public void crawlBhcMenu() throws Exception {
        System.out.println("BHC 크롤링 시작...");

        Map<Long, CrawledMenuData> crawledMenuMap = new LinkedHashMap<>();

        Document categoryDoc = Jsoup.connect(CATEGORY_LIST_URL)
                .ignoreContentType(true)
                .userAgent("Mozilla/5.0")
                .timeout(10000)
                .get();

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
                    CrawledMenuData existing = crawledMenuMap.get(sourceMenuId);
                    existing.addOriginalTag(topCateName);
                    addProductOriginalTags(existing, item);
                    addOriginalFlags(existing, item);
                    continue;
                }

                CrawledMenuData data = new CrawledMenuData(
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

        // 기존 BHC 메뉴 조회
        List<Menu> existingMenus = menuRepository.findByBrandName(BRAND_NAME);
        Map<Long, Menu> existingMenuMap = existingMenus.stream()
                .filter(menu -> menu.getSourceMenuId() != null)
                .collect(Collectors.toMap(Menu::getSourceMenuId, menu -> menu));

        Set<Long> crawledIds = crawledMenuMap.keySet();

        // upsert + ORIGINAL 태그 갱신
        for (CrawledMenuData data : crawledMenuMap.values()) {
            Menu menu;

            if (existingMenuMap.containsKey(data.getSourceMenuId())) {
                menu = existingMenuMap.get(data.getSourceMenuId());
                menu.updateMenuInfo(
                        data.getMenuName(),
                        data.getMenuPrice(),
                        data.getMenuImageUrl(),
                        data.getDescription()
                );
            } else {
                menu = new Menu(
                        data.getSourceMenuId(),
                        data.getMenuName(),
                        data.getMenuPrice(),
                        data.getMenuImageUrl(),
                        data.getDescription(),
                        data.getBrandName()
                );
            }

            Menu savedMenu = menuRepository.save(menu);

            // ORIGINAL 태그만 삭제 후 다시 연결
            menuTagMappingRepository.deleteByMenuIdAndTagTagType(savedMenu.getId(), TagType.ORIGINAL);
            menuTagMappingRepository.flush();
            
            for (String originalTagName : data.getOriginalTags()) {
                Tag originalTag = getOrCreateTag(originalTagName, TagType.ORIGINAL, BRAND_NAME);
                menuTagMappingRepository.save(new MenuTagMapping(savedMenu, originalTag));
            }
        }

        // 이번 크롤링 결과에 없는 기존 메뉴 삭제
        for (Menu existingMenu : existingMenus) {
            Long sourceMenuId = existingMenu.getSourceMenuId();
            if (sourceMenuId == null || !crawledIds.contains(sourceMenuId)) {
                menuRepository.delete(existingMenu);
            }
        }

        System.out.println("BHC 크롤링 완료! 저장 건수: " + crawledMenuMap.size());
    }

    /**
     * 치킨으로 볼 상위 카테고리만 허용
     */
    private boolean isChickenTopCategory(String topCateName) {
        return "치킨".equals(topCateName)
                || "신메뉴".equals(topCateName)
                || "BHC시그니처".equals(topCateName);
    }

    /**
     * 오로지 치킨 메뉴만 저장
     * 제외:
     * - 콜팝
     * - 세트류 / 혼치세트
     * - 라이스 / 볶음밥 결합 메뉴
     * - 치즈볼/콜라 결합 메뉴
     */
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

    /**
     * 상품 응답의 cateNm 배열을 ORIGINAL 태그로 저장
     */
    private void addProductOriginalTags(CrawledMenuData data, JsonNode item) {
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

    /**
     * BHC가 원래 주는 플래그는 SERVICE가 아니라 ORIGINAL 태그로 저장
     */
    private void addOriginalFlags(CrawledMenuData data, JsonNode item) {
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

    /**
     * 상세 API에서 가격 조회
     * 실패하면 0
     */
    private Integer fetchPriceByProductCd(Long productCd) {
        try {
            String detailUrl = DETAIL_API_URL_PREFIX + productCd;

            Document detailDoc = Jsoup.connect(detailUrl)
                    .ignoreContentType(true)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

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

    private Tag getOrCreateTag(String tagName, TagType tagType, String brandName) {
        return tagRepository.findByNameAndTagTypeAndBrandName(tagName, tagType, brandName)
                .orElseGet(() -> tagRepository.save(new Tag(tagName, tagType, brandName)));
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

    private static class CrawledMenuData {
        private final Long sourceMenuId;
        private final String menuName;
        private final Integer menuPrice;
        private final String menuImageUrl;
        private final String description;
        private final String brandName;
        private final LinkedHashSet<String> originalTags = new LinkedHashSet<>();

        public CrawledMenuData(Long sourceMenuId,
                               String menuName,
                               Integer menuPrice,
                               String menuImageUrl,
                               String description,
                               String brandName) {
            this.sourceMenuId = sourceMenuId;
            this.menuName = menuName;
            this.menuPrice = menuPrice;
            this.menuImageUrl = menuImageUrl;
            this.description = description;
            this.brandName = brandName;
        }

        public void addOriginalTag(String tagName) {
            if (tagName != null && !tagName.isBlank()) {
                this.originalTags.add(tagName);
            }
        }

        public Long getSourceMenuId() {
            return sourceMenuId;
        }

        public String getMenuName() {
            return menuName;
        }

        public Integer getMenuPrice() {
            return menuPrice;
        }

        public String getMenuImageUrl() {
            return menuImageUrl;
        }

        public String getDescription() {
            return description;
        }

        public String getBrandName() {
            return brandName;
        }

        public LinkedHashSet<String> getOriginalTags() {
            return originalTags;
        }
    }
}