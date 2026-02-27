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

@Service
@RequiredArgsConstructor
public class BBQCrawlerService {

    private final MenuRepository menuRepository;
    private final ObjectMapper objectMapper = new ObjectMapper(); // ✅ 여기만 다시 원복

    @Transactional
    public void crawlBbqMenu() throws Exception {
        System.out.println("BBQ 크롤링 시작...");

        Document doc = Jsoup.connect("https://www.bbq.co.kr/api/delivery/menu/2")
                .ignoreContentType(true)
                .get();

        JsonNode items = objectMapper.readTree(doc.body().text());

        menuRepository.deleteByBrandName("BBQ");

        for (JsonNode item : items) {
            Menu menu = new Menu(
                    item.get("menuName").asText(),
                    item.get("menuPrice").asInt(),
                    item.get("menuImageUrl").asText(),
                    item.get("description").asText(),
                    "BBQ"
            );
            menuRepository.save(menu);
        }

        System.out.println("BBQ 크롤링 완료!");
    }
}