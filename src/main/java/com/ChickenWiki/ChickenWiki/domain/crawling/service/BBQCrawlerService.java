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

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BBQCrawlerService {

    private static final String BRAND_NAME = "BBQ";

    private final MenuRepository menuRepository;
    private final TagRepository tagRepository;
    private final MenuTagMappingRepository menuTagMappingRepository;
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
        System.out.println("BBQ 크롤링 시작...");

        Map<Long, CrawledMenuData> crawledMenuMap = new LinkedHashMap<>();

        for (Map.Entry<Integer, String> entry : BBQ_CATEGORY_MAP.entrySet()) {
            Integer categoryId = entry.getKey();
            String originCategory = entry.getValue();

            String url = "https://www.bbq.co.kr/api/delivery/menu/" + categoryId;
            System.out.println("크롤링 중 URL: " + url);

            Document doc = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .get();

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
                    crawledMenuMap.get(sourceMenuId).addOriginCategory(originCategory);
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
                data.addOriginCategory(originCategory);

                crawledMenuMap.put(sourceMenuId, data);
            }
        }

        // 기존 BBQ 메뉴들 조회
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
            
            for (String originCategory : data.getOriginCategories()) {
                Tag tag = getOrCreateTag(originCategory, TagType.ORIGINAL, BRAND_NAME);
                menuTagMappingRepository.save(new MenuTagMapping(savedMenu, tag));
            }
        }

        // 이번 크롤링 결과에 없는 기존 메뉴 삭제
        for (Menu existingMenu : existingMenus) {
            Long sourceMenuId = existingMenu.getSourceMenuId();
            if (sourceMenuId == null || !crawledIds.contains(sourceMenuId)) {
                menuRepository.delete(existingMenu);
            }
        }

        System.out.println("BBQ 크롤링 완료! 저장 건수: " + crawledMenuMap.size());
    }

    /**
     * 저장 규칙
     * 1. menuType 이 SIDE 이면 제외
     * 2. menuName 에 "세트" 가 있으면 제외
     * 3. menuName 에 "치킨" 이 있으면 저장
     * 4. 없으면 origin 에 "닭고기" 가 있으면 저장
     * 5. 그것도 없으면 제외
     */
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

    private Tag getOrCreateTag(String tagName, TagType tagType, String brandName) {
        return tagRepository.findByNameAndTagTypeAndBrandName(tagName, tagType, brandName)
                .orElseGet(() -> tagRepository.save(new Tag(tagName, tagType, brandName)));
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

    private static class CrawledMenuData {
        private final Long sourceMenuId;
        private final String menuName;
        private final Integer menuPrice;
        private final String menuImageUrl;
        private final String description;
        private final String brandName;
        private final LinkedHashSet<String> originCategories = new LinkedHashSet<>();

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

        public void addOriginCategory(String category) {
            this.originCategories.add(category);
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

        public LinkedHashSet<String> getOriginCategories() {
            return originCategories;
        }
    }
}