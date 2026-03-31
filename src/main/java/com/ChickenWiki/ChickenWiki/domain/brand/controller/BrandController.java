package com.ChickenWiki.ChickenWiki.domain.brand.controller;

import com.ChickenWiki.ChickenWiki.domain.brand.dto.BrandDto;
import com.ChickenWiki.ChickenWiki.domain.brand.dto.MenuDto;
import com.ChickenWiki.ChickenWiki.domain.brand.service.BrandService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
public class BrandController {
    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @GetMapping
    public List<BrandDto> list() {
        return brandService.findAllBrands();
    }

    @GetMapping("/{id}")
    public BrandDto getBrand(@PathVariable Long id) {
        return brandService.findBrandById(id);
    }

    @GetMapping("/{id}/menus")
    public List<MenuDto> menus(@PathVariable Long id) {
        return brandService.findMenusByBrandId(id);
    }

    @GetMapping("/{id}/reviews")
    public List<com.ChickenWiki.ChickenWiki.domain.review.dto.ReviewDto> reviews(@PathVariable Long id) {
        return brandService.findTopReviewsByBrandId(id);
    }
}
