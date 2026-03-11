package com.ChickenWiki.ChickenWiki.domain.crawling.service;

import com.ChickenWiki.ChickenWiki.domain.brand.entity.Menu;
import com.ChickenWiki.ChickenWiki.domain.brand.repository.MenuRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BBQCrawlerService {

    private final MenuRepository menuRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public void crawlBbqMenu() throws Exception {
        System.out.println("BBQ 크롤링 시작...");

        List<Menu> menus = new ArrayList<>();

        for (int categoryId = 2; categoryId <= 6; categoryId++) {
            String url = "https://www.bbq.co.kr/api/delivery/menu/" + categoryId;
            System.out.println("크롤링 중 URL: " + url);

            Document doc = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .get();

            JsonNode items = objectMapper.readTree(doc.body().text());

            for (JsonNode item : items) {
                String menuName = getTextValue(item, "menuName");
                Integer menuPrice = getIntValue(item, "menuPrice");
                String menuImageUrl = getTextValue(item, "menuImageUrl");
                String description = getTextValue(item, "description");

                Menu menu = new Menu(
                        menuName,
                        menuPrice,
                        menuImageUrl,
                        description,
                        "BBQ"
                );

                menus.add(menu);
            }
        }

        menuRepository.deleteByBrandName("BBQ");
        menuRepository.saveAll(menus);

        System.out.println("BBQ 크롤링 완료! 저장 건수: " + menus.size());
    }

    private String getTextValue(JsonNode item, String fieldName) {
        JsonNode node = item.get(fieldName);
        return node != null && !node.isNull() ? node.asText() : "";
    }

    private Integer getIntValue(JsonNode item, String fieldName) {
        JsonNode node = item.get(fieldName);
        return node != null && !node.isNull() ? node.asInt() : 0;
    }
}