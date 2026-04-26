package com.ChickenWiki.ChickenWiki.domain.menu.service;

import com.ChickenWiki.ChickenWiki.domain.brand.dto.MenuDto;
import com.ChickenWiki.ChickenWiki.domain.brand.entity.Menu;
import com.ChickenWiki.ChickenWiki.domain.brand.repository.MenuRepository;
import com.ChickenWiki.ChickenWiki.domain.review.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;
    private final ReviewRepository reviewRepository;

    public MenuService(MenuRepository menuRepository, ReviewRepository reviewRepository) {
        this.menuRepository = menuRepository;
        this.reviewRepository = reviewRepository;
    }

    public List<MenuDto> findAllMenus() {
        List<Menu> menus = menuRepository.findAllByActiveTrue();
        if (menus.isEmpty()) {
            return List.of();
        }

        Map<Long, ReviewRepository.MenuReviewStatsProjection> statsByMenuId = reviewRepository
                .findMenuReviewStatsByMenuIds(menus.stream().map(Menu::getId).toList())
                .stream()
                .collect(Collectors.toMap(ReviewRepository.MenuReviewStatsProjection::getMenuId, stats -> stats));

        return menus.stream()
                .map(menu -> toDto(menu, statsByMenuId.get(menu.getId())))
                .collect(Collectors.toList());
    }

    public MenuDto findMenuById(Long menuId) {
        Menu menu = menuRepository.findByIdAndActiveTrue(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴가 없습니다: " + menuId));
        ReviewRepository.MenuReviewStatsProjection stats = reviewRepository.findMenuReviewStatsByMenuIds(List.of(menuId))
                .stream()
                .findFirst()
                .orElse(null);
        return toDto(menu, stats);
    }

    private MenuDto toDto(Menu m, ReviewRepository.MenuReviewStatsProjection stats) {
        return new MenuDto(m.getId(), m.getMenuName(), m.getMenuPrice(),
                m.getMenuImageUrl(), m.getDescription(), m.getBrandName(), m.getCrawledAt(),
                stats != null ? stats.getReviewCount() : 0L,
                stats != null && stats.getAverageRating() != null ? stats.getAverageRating() : 0.0);
    }
}
