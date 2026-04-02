package com.ChickenWiki.ChickenWiki.domain.review.service;

import com.ChickenWiki.ChickenWiki.domain.brand.repository.MenuRepository;
import com.ChickenWiki.ChickenWiki.domain.review.dto.ReviewCreateRequest;
import com.ChickenWiki.ChickenWiki.domain.review.dto.ReviewDto;
import com.ChickenWiki.ChickenWiki.domain.review.entity.Review;
import com.ChickenWiki.ChickenWiki.domain.review.repository.ReviewRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MenuRepository menuRepository;

    public ReviewService(ReviewRepository reviewRepository, MenuRepository menuRepository) {
        this.reviewRepository = reviewRepository;
        this.menuRepository = menuRepository;
    }

    public List<ReviewDto> findByMenuId(Long menuId) {
        ensureActiveMenu(menuId);
        return reviewRepository.findByMenuIdOrderByCreatedAtDesc(menuId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ReviewDto create(Long menuId, ReviewCreateRequest request) {
        ensureActiveMenu(menuId);

        String author = request.getAuthor() == null || request.getAuthor().isBlank()
                ? "Anonymous"
                : request.getAuthor().trim();

        String content = request.getContent() == null ? "" : request.getContent().trim();
        if (content.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Review content is required");
        }

        Integer rating = request.getRating();
        if (rating == null || rating < 1 || rating > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }

        Review review = reviewRepository.save(new Review(menuId, author, rating, content));
        return toDto(review);
    }

    private ReviewDto toDto(Review r) {
        return new ReviewDto(r.getId(), r.getMenuId(), r.getAuthor(), r.getRating(), r.getContent(), r.getCreatedAt());
    }

    private void ensureActiveMenu(Long menuId) {
        menuRepository.findByIdAndActiveTrue(menuId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "메뉴가 없습니다: " + menuId));
    }
}
