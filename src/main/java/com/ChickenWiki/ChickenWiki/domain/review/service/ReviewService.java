package com.ChickenWiki.ChickenWiki.domain.review.service;

import com.ChickenWiki.ChickenWiki.domain.brand.repository.MenuRepository;
import com.ChickenWiki.ChickenWiki.domain.review.dto.ReviewCreateRequest;
import com.ChickenWiki.ChickenWiki.domain.review.dto.ReviewDto;
import com.ChickenWiki.ChickenWiki.domain.review.entity.Review;
import com.ChickenWiki.ChickenWiki.domain.review.repository.ReviewRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
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

    public ReviewDto create(Long menuId, ReviewCreateRequest request, String author) {
        ensureActiveMenu(menuId);

        Review review = reviewRepository.save(
                new Review(menuId, author, validateRating(request.getRating()), validateContent(request.getContent()))
        );
        return toDto(review);
    }

    public ReviewDto update(Long menuId, Long reviewId, ReviewCreateRequest request, String author) {
        ensureActiveMenu(menuId);

        Review review = findReview(reviewId);
        validateMenu(review, menuId);
        validateAuthor(review, author);

        review.update(validateRating(request.getRating()), validateContent(request.getContent()));
        return toDto(reviewRepository.save(review));
    }

    public void delete(Long menuId, Long reviewId, String author) {
        ensureActiveMenu(menuId);

        Review review = findReview(reviewId);
        validateMenu(review, menuId);
        validateAuthor(review, author);
        reviewRepository.delete(review);
    }

    private Review findReview(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."));
    }

    private void validateMenu(Review review, Long menuId) {
        if (!review.getMenuId().equals(menuId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "메뉴와 리뷰 정보가 일치하지 않습니다.");
        }
    }

    private void validateAuthor(Review review, String author) {
        if (!review.getAuthor().equals(author)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인이 작성한 리뷰만 수정하거나 삭제할 수 있습니다.");
        }
    }

    private String validateContent(String rawContent) {
        String content = rawContent == null ? "" : rawContent.trim();
        if (content.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Review content is required");
        }
        return content;
    }

    private Integer validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }
        return rating;
    }

    private ReviewDto toDto(Review review) {
        return new ReviewDto(
                review.getId(),
                review.getMenuId(),
                review.getAuthor(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt()
        );
    }

    private void ensureActiveMenu(Long menuId) {
        menuRepository.findByIdAndActiveTrue(menuId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "메뉴를 찾을 수 없습니다: " + menuId));
    }
}
