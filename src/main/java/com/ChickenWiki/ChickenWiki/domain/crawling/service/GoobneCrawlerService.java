package com.ChickenWiki.ChickenWiki.domain.crawling.service;

import com.ChickenWiki.ChickenWiki.domain.brand.entity.Menu;
import com.ChickenWiki.ChickenWiki.domain.brand.entity.MenuTagMapping;
import com.ChickenWiki.ChickenWiki.domain.brand.entity.Tag;
import com.ChickenWiki.ChickenWiki.domain.brand.entity.TagType;
import com.ChickenWiki.ChickenWiki.domain.brand.repository.MenuRepository;
import com.ChickenWiki.ChickenWiki.domain.brand.repository.MenuTagMappingRepository;
import com.ChickenWiki.ChickenWiki.domain.brand.repository.TagRepository;
import lombok.RequiredArgsConstructor;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoobneCrawlerService {

    private static final String BRAND_NAME = "굽네치킨";
    private static final String BASE_URL = "https://www.goobne.co.kr";
    private static final String LIST_URL = BASE_URL + "/menu/menu_list_p";
    private static final String DETAIL_URL = BASE_URL + "/menu/menu_view_p";

    /**
     * 수집 대상:
     * 1) 치킨 > 오븐류
     * 2) 치킨 > 양념류
     * 3) 치킨 > 기타류
     * 4) 신제품 > 치킨
     */
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

    private final MenuRepository menuRepository;
    private final TagRepository tagRepository;
    private final MenuTagMappingRepository menuTagMappingRepository;

    @Transactional
    public void crawlGoobneMenu() throws Exception {
        System.out.println("굽네치킨 크롤링 시작...");

        Map<Long, CrawledMenuData> crawledMenuMap = new LinkedHashMap<>();

        for (CategoryPair pair : CATEGORY_PAIRS) {
            String url = LIST_URL
                    + "?classId=" + encode(pair.classId)
                    + "&classId2=" + encode(pair.classId2)
                    + "&itemId="
                    + "&source="
                    + "&quickBrId="
                    + "&quickDeliverySeq="
                    + "&dlvType=";

            System.out.println("굽네 목록 크롤링 URL: " + url);

            Document listDoc;
            try {
                listDoc = createHtmlConnection(url, LIST_URL).get();
            } catch (Exception e) {
                System.out.println("[LIST FAIL] url=" + url + ", error=" + e.getMessage());
                continue;
            }

            Elements items = listDoc.select(".menu-grid a.item");
            System.out.println("목록 개수: " + items.size());

            for (Element item : items) {
                String itemIdText = extractItemId(item.attr("onclick"));
                Long sourceMenuId = parseLongSafe(itemIdText);

                String menuName = extractMenuName(item);
                Integer menuPrice = extractMenuPrice(item);
                String menuImageUrl = extractMenuImageUrl(item);
                String description = "";

                if (sourceMenuId == null || menuName.isBlank()) {
                    System.out.println("[SKIP] itemId 또는 menuName 비어있음");
                    continue;
                }

                try {
                    String detailUrl = DETAIL_URL + "?itemId=" + encode(itemIdText);
                    Document detailDoc = createHtmlConnection(detailUrl, url).get();

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

                    System.out.println("[DETAIL] itemId=" + itemIdText + ", menuName=" + menuName);
                } catch (Exception e) {
                    System.out.println("[DETAIL FAIL] itemId=" + itemIdText + ", error=" + e.getMessage());
                }

                menuName = cleanText(menuName);
                menuImageUrl = cleanText(menuImageUrl);
                description = cleanDescription(description);

                if (!shouldSaveMenu(menuName, description, pair)) {
                    System.out.println("[FILTERED] itemId=" + itemIdText + ", menuName=" + menuName);
                    continue;
                }

                if (crawledMenuMap.containsKey(sourceMenuId)) {
                    CrawledMenuData existing = crawledMenuMap.get(sourceMenuId);
                    existing.addOriginalTag(pair.mainCategoryName);
                    existing.addOriginalTag(pair.subCategoryName);
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
                data.addOriginalTag(pair.mainCategoryName);
                data.addOriginalTag(pair.subCategoryName);

                crawledMenuMap.put(sourceMenuId, data);
                System.out.println("[ADD] itemId=" + itemIdText + ", menuName=" + menuName + ", price=" + menuPrice);
            }
        }

        // 기존 굽네 메뉴 조회
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

        System.out.println("굽네치킨 크롤링 완료! 저장 건수: " + crawledMenuMap.size());
    }

    /**
     * 저장 규칙
     * - 세트 제외
     * - 현재 수집 대상 카테고리 자체가 치킨 범주만 오도록 구성됨
     */
    private boolean shouldSaveMenu(String menuName, String description, CategoryPair pair) {
        String merged = (menuName + " " + description).replace("\n", " ");

        if (merged.contains("세트") || merged.contains("SET")) {
            return false;
        }

        return true;
    }

    private Tag getOrCreateTag(String tagName, TagType tagType, String brandName) {
        return tagRepository.findByNameAndTagTypeAndBrandName(tagName, tagType, brandName)
                .orElseGet(() -> tagRepository.save(new Tag(tagName, tagType, brandName)));
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

        // 예: menu_view("40796", 0)
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