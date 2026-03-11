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
public class BHCCrawlerService {

    private static final String BRAND_NAME = "BHC";
    private static final String LIST_API_URL = "https://www.bhc.co.kr/api/v1/web/categories/1/products";
    private static final String DETAIL_API_URL_PREFIX = "https://www.bhc.co.kr/api/v1/web/products/";

    private final MenuRepository menuRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public void crawlBhcMenu() throws Exception {
        System.out.println("BHC 크롤링 시작...");

        Document listDoc = Jsoup.connect(LIST_API_URL)
                .ignoreContentType(true)
                .userAgent("Mozilla/5.0")
                .timeout(10000)
                .get();

        JsonNode listRoot = objectMapper.readTree(listDoc.body().text());
        JsonNode listBody = listRoot.get("body");

        List<Menu> menus = new ArrayList<>();

        if (listBody == null || !listBody.isArray()) {
            throw new IllegalStateException("BHC 목록 응답 body가 배열이 아닙니다.");
        }

        for (JsonNode item : listBody) {
            String productCd = getTextValue(item, "productCd");
            String menuNameFromList = cleanText(getTextValue(item, "productNm"));
            String descriptionFromList = getDescriptionFromList(item);
            String imageFromList = getTextValue(item, "mainImg");

            if (productCd.isBlank() || menuNameFromList.isBlank()) {
                continue;
            }

            String detailUrl = DETAIL_API_URL_PREFIX + productCd;

            Document detailDoc = Jsoup.connect(detailUrl)
                    .ignoreContentType(true)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            JsonNode detailRoot = objectMapper.readTree(detailDoc.body().text());
            JsonNode detailBody = detailRoot.get("body");

            String menuName = menuNameFromList;
            Integer menuPrice = 0;
            String menuImageUrl = imageFromList;
            String description = descriptionFromList;

            if (detailBody != null && !detailBody.isNull()) {
                String menuNameFromDetail = cleanText(getTextValue(detailBody, "productNm"));
                Integer priceFromDetail = getIntValue(detailBody, "price");
                String imageFromDetail = getTextValue(detailBody, "mainImg");
                String descriptionFromDetail = getDescriptionFromDetail(detailBody);

                if (!menuNameFromDetail.isBlank()) {
                    menuName = menuNameFromDetail;
                }

                menuPrice = priceFromDetail;

                if (!imageFromDetail.isBlank()) {
                    menuImageUrl = imageFromDetail;
                }

                if (!descriptionFromDetail.isBlank()) {
                    description = descriptionFromDetail;
                }
            }

            Menu menu = new Menu(
                    menuName,
                    menuPrice,
                    cleanText(menuImageUrl),
                    description,
                    BRAND_NAME
            );

            menus.add(menu);
        }

        menuRepository.deleteByBrandName(BRAND_NAME);
        menuRepository.saveAll(menus);

        System.out.println("BHC 크롤링 완료! 저장 건수: " + menus.size());
    }

    private String getDescriptionFromList(JsonNode item) {
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

    private String getDescriptionFromDetail(JsonNode detailBody) {
        String mobileDetailDescription = getTextValue(detailBody, "mobileDetailDescription");
        if (!mobileDetailDescription.isBlank()) {
            return cleanTextPreserveLineBreaks(mobileDetailDescription);
        }

        String mobileListDescription = getTextValue(detailBody, "mobileListDescription");
        if (!mobileListDescription.isBlank()) {
            return cleanTextPreserveLineBreaks(mobileListDescription);
        }

        String description = getTextValue(detailBody, "description");
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