package com.ChickenWiki.ChickenWiki.domain.brand.service;

import com.ChickenWiki.ChickenWiki.domain.brand.dto.BrandDto;
import com.ChickenWiki.ChickenWiki.domain.brand.dto.MenuDto;
import com.ChickenWiki.ChickenWiki.domain.brand.entity.Brand;
import com.ChickenWiki.ChickenWiki.domain.brand.entity.Menu;
import com.ChickenWiki.ChickenWiki.domain.brand.repository.BrandRepository;
import com.ChickenWiki.ChickenWiki.domain.brand.repository.MenuRepository;
import com.ChickenWiki.ChickenWiki.domain.review.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BrandService {

    private final BrandRepository brandRepository;
    private final MenuRepository menuRepository;
    private final ReviewRepository reviewRepository;

    public BrandService(BrandRepository brandRepository, MenuRepository menuRepository, ReviewRepository reviewRepository) {
        this.brandRepository = brandRepository;
        this.menuRepository = menuRepository;
        this.reviewRepository = reviewRepository;
    }

    public List<BrandDto> findAllBrands() {
        return brandRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public BrandDto findBrandById(Long brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new IllegalArgumentException("브랜드가 없습니다: " + brandId));
        return toDto(brand);
    }

    public List<MenuDto> findMenusByBrandId(Long brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new IllegalArgumentException("브랜드가 없습니다: " + brandId));
        List<Menu> menus = menuRepository.findByBrandNameAndActiveTrue(brand.getName());
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

    public List<com.ChickenWiki.ChickenWiki.domain.review.dto.ReviewDto> findTopReviewsByBrandId(Long brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new IllegalArgumentException("브랜드가 없습니다: " + brandId));
        List<Menu> menus = menuRepository.findByBrandNameAndActiveTrue(brand.getName());
        if (menus.isEmpty()) {
            return List.of();
        }
        List<Long> menuIds = menus.stream().map(Menu::getId).collect(Collectors.toList());
        List<com.ChickenWiki.ChickenWiki.domain.review.entity.Review> reviews =
                reviewRepository.findByMenuIdIn(menuIds);
        return reviews.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(3)
                .map(r -> new com.ChickenWiki.ChickenWiki.domain.review.dto.ReviewDto(
                        r.getId(), r.getMenuId(), r.getAuthor(), r.getRating(), r.getContent(), 0L, false, r.getCreatedAt()))
                .collect(Collectors.toList());
    }

    private BrandDto toDto(Brand b) {
        return new BrandDto(b.getId(), b.getName(), b.getLogoUrl());
    }

    private MenuDto toDto(Menu m, ReviewRepository.MenuReviewStatsProjection stats) {
        return new MenuDto(m.getId(), m.getMenuName(), m.getMenuPrice(),
                m.getMenuImageUrl(), m.getDescription(), m.getBrandName(), m.getCrawledAt(),
                stats != null ? stats.getReviewCount() : 0L,
                stats != null && stats.getAverageRating() != null ? stats.getAverageRating() : 0.0);
    }
}
