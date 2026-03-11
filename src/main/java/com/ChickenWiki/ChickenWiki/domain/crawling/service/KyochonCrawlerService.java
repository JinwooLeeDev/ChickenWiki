package com.ChickenWiki.ChickenWiki.domain.crawling.service;

import com.ChickenWiki.ChickenWiki.domain.brand.entity.Menu;
import com.ChickenWiki.ChickenWiki.domain.brand.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KyochonCrawlerService {

    private static final String BRAND_NAME = "교촌치킨";
    private static final String BASE_URL = "https://www.kyochon.com";
    private static final String MENU_URL = "https://www.kyochon.com/menu/chicken.asp";

    private final MenuRepository menuRepository;

    @Transactional
    public void crawlKyochonMenu() throws Exception {
        System.out.println("교촌치킨 크롤링 시작...");

        Document doc = Jsoup.connect(MENU_URL)
                .userAgent("Mozilla/5.0")
                .timeout(10000)
                .get();

        Elements menuElements = doc.select("ul.menuProduct > li");
        List<Menu> menus = new ArrayList<>();

        for (Element element : menuElements) {
            String menuName = extractMenuName(element);
            Integer menuPrice = extractMenuPrice(element);
            String menuImageUrl = extractMenuImageUrl(element);
            String description = extractDescription(element);

            if (menuName.isBlank()) {
                continue;
            }

            Menu menu = new Menu(
                    menuName,
                    menuPrice,
                    menuImageUrl,
                    description,
                    BRAND_NAME
            );

            menus.add(menu);
        }

        menuRepository.deleteByBrandName(BRAND_NAME);
        menuRepository.saveAll(menus);

        System.out.println("교촌치킨 크롤링 완료! 저장 건수: " + menus.size());
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

        return cleanText(
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
}