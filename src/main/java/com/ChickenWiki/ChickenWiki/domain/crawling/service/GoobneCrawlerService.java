package com.ChickenWiki.ChickenWiki.domain.crawling.service;

import com.ChickenWiki.ChickenWiki.domain.brand.entity.Menu;
import com.ChickenWiki.ChickenWiki.domain.brand.repository.MenuRepository;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GoobneCrawlerService {

    private static final String BRAND_NAME = "굽네치킨";
    private static final String BASE_URL = "https://www.goobne.co.kr";
    private static final String LIST_URL = BASE_URL + "/menu/menu_list_p";
    private static final String DETAIL_URL = BASE_URL + "/menu/menu_view_p";

    // 치킨(classId=1) 안의 소분류
    // 오븐류=2, 양념류=7, 기타류=27
    private static final String[][] CHICKEN_CATEGORY_PAIRS = {
            {"1", "2"},
            {"1", "7"},
            {"1", "27"}
    };

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/144.0.0.0 Safari/537.36";

    private final MenuRepository menuRepository;

    @Transactional
    public void crawlGoobneMenu() throws Exception {
        System.out.println("굽네치킨 크롤링 시작...");

        // itemId 기준 중복 제거
        Map<String, Menu> menuMap = new LinkedHashMap<>();

        for (String[] pair : CHICKEN_CATEGORY_PAIRS) {
            String classId = pair[0];
            String classId2 = pair[1];

            String url = LIST_URL
                    + "?classId=" + encode(classId)
                    + "&classId2=" + encode(classId2);

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
                String itemId = extractItemId(item.attr("onclick"));
                String menuName = extractMenuName(item);
                Integer menuPrice = extractMenuPrice(item);
                String menuImageUrl = extractMenuImageUrl(item);
                String description = "";

                if (itemId.isBlank() || menuName.isBlank()) {
                    System.out.println("[SKIP] itemId 또는 menuName 비어있음");
                    continue;
                }

                try {
                    String detailUrl = DETAIL_URL + "?itemId=" + encode(itemId);
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

                    System.out.println("[DETAIL] itemId=" + itemId + ", menuName=" + menuName);
                } catch (Exception e) {
                    System.out.println("[DETAIL FAIL] itemId=" + itemId + ", error=" + e.getMessage());
                }

                Menu menu = new Menu(
                        cleanText(menuName),
                        menuPrice,
                        cleanText(menuImageUrl),
                        cleanDescription(description),
                        BRAND_NAME
                );

                menuMap.put(itemId, menu);
                System.out.println("[ADD] itemId=" + itemId + ", menuName=" + menuName + ", price=" + menuPrice);
            }
        }

        List<Menu> menus = new ArrayList<>(menuMap.values());

        menuRepository.deleteByBrandName(BRAND_NAME);
        menuRepository.saveAll(menus);

        System.out.println("굽네치킨 크롤링 완료! 저장 건수: " + menus.size());
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
}