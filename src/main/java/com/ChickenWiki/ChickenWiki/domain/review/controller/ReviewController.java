package com.ChickenWiki.ChickenWiki.domain.review.controller;

import com.ChickenWiki.ChickenWiki.domain.review.dto.ReviewCreateRequest;
import com.ChickenWiki.ChickenWiki.domain.review.dto.ReviewDto;
import com.ChickenWiki.ChickenWiki.domain.review.service.ReviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menus/{menuId}/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public List<ReviewDto> list(@PathVariable Long menuId) {
        return reviewService.findByMenuId(menuId);
    }

    @PostMapping
    public ReviewDto create(@PathVariable Long menuId, @RequestBody ReviewCreateRequest request) {
        return reviewService.create(menuId, request);
    }
}
