package com.ChickenWiki.ChickenWiki.domain.crawling.service;

import com.ChickenWiki.ChickenWiki.domain.crawling.model.CrawledMenuSnapshot;
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
import java.util.Map;

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

    private final MenuCrawlSyncService menuCrawlSyncService;
    private final CrawlRequestDelayService crawlRequestDelayService;

    @Transactional
    public void crawlKyochonMenu() throws Exception {
        System.out.println("교촌치킨 크롤링 시작...");

        Map<Long, CrawledMenuSnapshot> crawledMenuMap = new LinkedHashMap<>();

        for (CategoryCode categoryCode : CATEGORY_CODES) {
            String url = MENU_URL + "?code=" + encode(categoryCode.code);
            System.out.println("교촌 크롤링 URL: " + url);

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();
            crawlRequestDelayService.pause();

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

                CrawledMenuSnapshot data = new CrawledMenuSnapshot(
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

        menuCrawlSyncService.sync(BRAND_NAME, crawledMenuMap.values());
        System.out.println("교촌치킨 크롤링 완료! 저장 건수: " + crawledMenuMap.size());
    }

    private boolean shouldSaveMenu(String menuName, String description) {
        String merged = (menuName + " " + description).replace("\n", " ");

        if (merged.contains("세트") || merged.contains("SET")) {
            return false;
        }

        return true;
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
}
