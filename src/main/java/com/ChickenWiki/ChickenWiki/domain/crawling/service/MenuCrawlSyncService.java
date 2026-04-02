package com.ChickenWiki.ChickenWiki.domain.crawling.service;

import com.ChickenWiki.ChickenWiki.domain.brand.entity.Menu;
import com.ChickenWiki.ChickenWiki.domain.brand.entity.MenuTagMapping;
import com.ChickenWiki.ChickenWiki.domain.brand.entity.Tag;
import com.ChickenWiki.ChickenWiki.domain.brand.entity.TagType;
import com.ChickenWiki.ChickenWiki.domain.brand.repository.MenuRepository;
import com.ChickenWiki.ChickenWiki.domain.brand.repository.MenuTagMappingRepository;
import com.ChickenWiki.ChickenWiki.domain.brand.repository.TagRepository;
import com.ChickenWiki.ChickenWiki.domain.crawling.model.CrawledMenuSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuCrawlSyncService {

    private final MenuRepository menuRepository;
    private final TagRepository tagRepository;
    private final MenuTagMappingRepository menuTagMappingRepository;

    @Transactional
    public void sync(String brandName, Collection<CrawledMenuSnapshot> crawledMenus) {
        if (crawledMenus == null || crawledMenus.isEmpty()) {
            throw new IllegalStateException(brandName + " 크롤링 결과가 비어 있어 동기화를 중단합니다.");
        }

        List<Menu> existingMenus = menuRepository.findByBrandName(brandName);
        Map<Long, Menu> existingMenuMap = existingMenus.stream()
                .filter(menu -> menu.getSourceMenuId() != null)
                .collect(Collectors.toMap(Menu::getSourceMenuId, menu -> menu, (left, right) -> left));
        Set<Long> crawledSourceMenuIds = new HashSet<>();

        for (CrawledMenuSnapshot crawledMenu : crawledMenus) {
            crawledSourceMenuIds.add(crawledMenu.getSourceMenuId());
            Menu menu = existingMenuMap.get(crawledMenu.getSourceMenuId());

            if (menu == null) {
                menu = menuRepository.save(new Menu(
                        crawledMenu.getSourceMenuId(),
                        crawledMenu.getMenuName(),
                        crawledMenu.getMenuPrice(),
                        crawledMenu.getMenuImageUrl(),
                        crawledMenu.getDescription(),
                        crawledMenu.getBrandName()
                ));
            } else if (!menu.hasSameContent(
                    crawledMenu.getMenuName(),
                    crawledMenu.getMenuPrice(),
                    crawledMenu.getMenuImageUrl(),
                    crawledMenu.getDescription())) {
                menu.updateMenuInfo(
                        crawledMenu.getMenuName(),
                        crawledMenu.getMenuPrice(),
                        crawledMenu.getMenuImageUrl(),
                        crawledMenu.getDescription()
                );
                menu = menuRepository.save(menu);
            } else if (!menu.isActive()) {
                menu.reactivate();
                menu = menuRepository.save(menu);
            }

            for (String originalTagName : crawledMenu.getOriginalTags()) {
                Tag tag = getOrCreateTag(originalTagName, TagType.ORIGINAL, brandName);
                if (!menuTagMappingRepository.existsByMenuIdAndTagId(menu.getId(), tag.getId())) {
                    menuTagMappingRepository.save(new MenuTagMapping(menu, tag));
                }
            }
        }

        for (Menu existingMenu : existingMenus) {
            if (existingMenu.getSourceMenuId() == null) {
                continue;
            }
            if (!crawledSourceMenuIds.contains(existingMenu.getSourceMenuId()) && existingMenu.isActive()) {
                existingMenu.deactivate();
                menuRepository.save(existingMenu);
            }
        }
    }

    private Tag getOrCreateTag(String tagName, TagType tagType, String brandName) {
        return tagRepository.findByNameAndTagTypeAndBrandName(tagName, tagType, brandName)
                .orElseGet(() -> tagRepository.save(new Tag(tagName, tagType, brandName)));
    }
}
