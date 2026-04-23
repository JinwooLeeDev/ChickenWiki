package com.ChickenWiki.ChickenWiki.domain.review.controller;

import com.ChickenWiki.ChickenWiki.domain.review.dto.ReviewCreateRequest;
import com.ChickenWiki.ChickenWiki.domain.review.dto.ReviewDto;
import com.ChickenWiki.ChickenWiki.domain.review.service.ReviewService;
import com.ChickenWiki.ChickenWiki.domain.user.entity.User;
import com.ChickenWiki.ChickenWiki.domain.user.service.UserSessionService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menus/{menuId}/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    private final UserSessionService userSessionService;

    public ReviewController(ReviewService reviewService, UserSessionService userSessionService) {
        this.reviewService = reviewService;
        this.userSessionService = userSessionService;
    }

    @GetMapping
    public List<ReviewDto> list(@PathVariable Long menuId) {
        return reviewService.findByMenuId(menuId);
    }

    @PostMapping
    public ReviewDto create(@PathVariable Long menuId,
                            @RequestBody ReviewCreateRequest request,
                            @RequestHeader("Authorization") String authorizationHeader) {
        User currentUser = userSessionService.getUserByAuthorizationHeader(authorizationHeader);
        return reviewService.create(menuId, request, currentUser.getNickname());
    }

    @PutMapping("/{reviewId}")
    public ReviewDto update(@PathVariable Long menuId,
                            @PathVariable Long reviewId,
                            @RequestBody ReviewCreateRequest request,
                            @RequestHeader("Authorization") String authorizationHeader) {
        User currentUser = userSessionService.getUserByAuthorizationHeader(authorizationHeader);
        return reviewService.update(menuId, reviewId, request, currentUser.getNickname());
    }

    @DeleteMapping("/{reviewId}")
    public void delete(@PathVariable Long menuId,
                       @PathVariable Long reviewId,
                       @RequestHeader("Authorization") String authorizationHeader) {
        User currentUser = userSessionService.getUserByAuthorizationHeader(authorizationHeader);
        reviewService.delete(menuId, reviewId, currentUser.getNickname());
    }
}
