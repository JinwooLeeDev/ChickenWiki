package com.ChickenWiki.ChickenWiki.domain.crawling.service;

import com.ChickenWiki.ChickenWiki.domain.brand.entity.Menu;
import com.ChickenWiki.ChickenWiki.domain.brand.entity.MenuTagMapping;
import com.ChickenWiki.ChickenWiki.domain.brand.entity.Tag;
import com.ChickenWiki.ChickenWiki.domain.brand.entity.TagType;
import com.ChickenWiki.ChickenWiki.domain.brand.repository.MenuRepository;
import com.ChickenWiki.ChickenWiki.domain.brand.repository.MenuTagMappingRepository;
import com.ChickenWiki.ChickenWiki.domain.brand.repository.TagRepository;
import lombok.RequiredArgsConstructor;
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
public class KyochonCrawlerService {

    private static final String BRAND_NAME = "교촌치킨";
    private static final String BASE_URL = "https://www.kyochon.com";
    private static final String MENU_URL = "https://www.kyochon.com/menu/chicken.asp";

    private static final CategoryCode[] CATEGORY_CODES = {
            new CategoryCode("21", "신메뉴"),
            new CategoryCode("19", "윙박스시리즈"),
            new CategoryCode("18", "싱글윙시리즈"),
            new CategoryCode("4", "허니시리즈"),
            new CategoryCode("5", "후라이드시리즈"),
            new CategoryCode("17", "허니옥수수"),
            new CategoryCode("1", "간장시리즈"),
            new CategoryCode("2", "레드시리즈"),
            new CategoryCode("8|3", "반반시리즈"),
            new CategoryCode("20", "살살시리즈")
    };

    private final MenuRepository menuRepository;
    private final TagRepository tagRepository;
    private final MenuTagMappingRepository menuTagMappingRepository;

    @Transactional
    public void crawlKyochonMenu() throws Exception {
        System.out.println("교촌치킨 크롤링 시작...");

        Map<Long, CrawledMenuData> crawledMenuMap = new LinkedHashMap<>();

        for (CategoryCode categoryCode : CATEGORY_CODES) {
            String url = MENU_URL + "?code=" + encode(categoryCode.code);
            System.out.println("교촌 크롤링 URL: " + url);

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            Elements menuElements = doc.select("ul.menuProduct > li");

            for (Element element : menuElements) {
                Long sourceMenuId = extractSourceMenuId(element);
                String menuName = extractMenuName(element);
                Integer menuPrice = extractMenuPrice(element);
                String menuImageUrl = extractMenuImageUrl(element);
                String description = extractDescription(element);

                if (sourceMenuId == null || menuName.isBlank()) {
                    continue;
                }

                if (!shouldSaveMenu(menuName, description)) {
                    continue;
                }

                if (crawledMenuMap.containsKey(sourceMenuId)) {
                    crawledMenuMap.get(sourceMenuId).addOriginalTag(categoryCode.label);
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
                data.addOriginalTag(categoryCode.label);

                crawledMenuMap.put(sourceMenuId, data);
            }
        }

        // 기존 교촌 메뉴 조회
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

        System.out.println("교촌치킨 크롤링 완료! 저장 건수: " + crawledMenuMap.size());
    }

    /**
     * 현재 수집 대상은 전부 치킨 시리즈이므로,
     * 세트성 메뉴만 제외한다.
     */
    private boolean shouldSaveMenu(String menuName, String description) {
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

    private Long extractSourceMenuId(Element element) {
        Element link = element.selectFirst("a[href]");
        if (link == null) {
            return null;
        }

        String href = link.attr("href");
        if (href == null || href.isBlank()) {
            return null;
        }

        // 예: view.asp?id=41130&cg=2
        int idIndex = href.indexOf("id=");
        if (idIndex == -1) {
            return null;
        }

        String idPart = href.substring(idIndex + 3);
        int ampIndex = idPart.indexOf("&");
        if (ampIndex != -1) {
            idPart = idPart.substring(0, ampIndex);
        }

        try {
            return Long.parseLong(idPart.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String extractMenuName(Element element) {
        Element nameElement = element.selectFirst(".txt dt");
        return nameElement != null ? cleanText(nameElement.text()) : "";
    }

    private Integer extractMenuPrice(Element element) {
        Element priceElement = element.selectFirst(".money strong");

        if (priceElement == null) {
            return 0;
        }

        String priceText = priceElement.text().replaceAll("[^0-9]", "");
        if (priceText.isBlank()) {
            return 0;
        }

        try {
            return Integer.parseInt(priceText);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String extractMenuImageUrl(Element element) {
        Element imageElement = element.selectFirst(".img img");

        if (imageElement == null) {
            return "";
        }

        String src = imageElement.attr("src");
        if (src == null || src.isBlank()) {
            return "";
        }

        if (src.startsWith("http://") || src.startsWith("https://")) {
            return src;
        }

        if (src.startsWith("/")) {
            return BASE_URL + src;
        }

        return BASE_URL + "/" + src;
    }

    private String extractDescription(Element element) {
        Element descriptionElement = element.selectFirst(".txt dd");

        if (descriptionElement == null) {
            return "";
        }

        String html = descriptionElement.html();

        return cleanDescription(
                html.replace("<br>", "\n")
                        .replace("<br/>", "\n")
                        .replace("<br />", "\n")
        );
    }

    private String cleanText(String text) {
        if (text == null) {
            return "";
        }

        return text.replace("&nbsp;", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String cleanDescription(String text) {
        if (text == null) {
            return "";
        }

        return text.replace("&nbsp;", " ")
                .replaceAll("[ \t]+", " ")
                .replaceAll("\n{2,}", "\n")
                .trim();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static class CategoryCode {
        private final String code;
        private final String label;

        public CategoryCode(String code, String label) {
            this.code = code;
            this.label = label;
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