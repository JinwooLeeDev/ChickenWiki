package com.ChickenWiki.ChickenWiki.domain.review.service;

import com.ChickenWiki.ChickenWiki.domain.brand.repository.MenuRepository;
import com.ChickenWiki.ChickenWiki.domain.review.dto.ReviewCreateRequest;
import com.ChickenWiki.ChickenWiki.domain.review.dto.ReviewDto;
import com.ChickenWiki.ChickenWiki.domain.review.entity.Review;
import com.ChickenWiki.ChickenWiki.domain.review.entity.ReviewLike;
import com.ChickenWiki.ChickenWiki.domain.review.repository.ReviewLikeRepository;
import com.ChickenWiki.ChickenWiki.domain.review.repository.ReviewRepository;
import com.ChickenWiki.ChickenWiki.domain.user.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final MenuRepository menuRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            ReviewLikeRepository reviewLikeRepository,
            MenuRepository menuRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.reviewLikeRepository = reviewLikeRepository;
        this.menuRepository = menuRepository;
    }

    public List<ReviewDto> findByMenuId(Long menuId, User currentUser) {
        ensureActiveMenu(menuId);
        List<Review> reviews = reviewRepository.findByMenuIdOrderByCreatedAtDesc(menuId);
        Map<Long, Long> likeCounts = getLikeCounts(reviews);
        Set<Long> likedReviewIds = getLikedReviewIds(reviews, currentUser);

        return reviews.stream()
                .map(review -> toDto(
                        review,
                        likeCounts.getOrDefault(review.getId(), 0L),
                        likedReviewIds.contains(review.getId())
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public ReviewDto create(Long menuId, ReviewCreateRequest request, String author) {
        ensureActiveMenu(menuId);

        Review review = reviewRepository.save(
                new Review(menuId, author, validateRating(request.getRating()), validateContent(request.getContent()))
        );
        return toDto(review, 0L, false);
    }

    @Transactional
    public ReviewDto update(Long menuId, Long reviewId, ReviewCreateRequest request, String author) {
        ensureActiveMenu(menuId);

        Review review = findReview(reviewId);
        validateMenu(review, menuId);
        validateAuthor(review, author);

        review.update(validateRating(request.getRating()), validateContent(request.getContent()));
        Review updatedReview = reviewRepository.save(review);
        return toDto(updatedReview, reviewLikeRepository.countByReviewId(updatedReview.getId()), false);
    }

    @Transactional
    public ReviewDto recommend(Long menuId, Long reviewId, User currentUser) {
        ensureActiveMenu(menuId);

        Review review = findReview(reviewId);
        validateMenu(review, menuId);

        boolean alreadyLiked = reviewLikeRepository.existsByUserIdAndReviewId(currentUser.getId(), reviewId);

        if (alreadyLiked) {
            reviewLikeRepository.deleteByUserIdAndReviewId(currentUser.getId(), reviewId);
            return toDto(review, reviewLikeRepository.countByReviewId(reviewId), false);
        }

        reviewLikeRepository.save(new ReviewLike(currentUser.getId(), reviewId));
        return toDto(review, reviewLikeRepository.countByReviewId(reviewId), true);
    }

    @Transactional
    public void delete(Long menuId, Long reviewId, User currentUser) {
        ensureActiveMenu(menuId);

        Review review = findReview(reviewId);
        validateMenu(review, menuId);
        validateDeletePermission(review, currentUser);
        reviewLikeRepository.deleteByReviewId(reviewId);
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

    private void validateDeletePermission(Review review, User currentUser) {
        boolean isOwner = review.getAuthor().equals(currentUser.getNickname());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());

        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "리뷰를 삭제할 권한이 없습니다.");
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

    private ReviewDto toDto(Review review, Long likeCount, boolean likedByCurrentUser) {
        return new ReviewDto(
                review.getId(),
                review.getMenuId(),
                review.getAuthor(),
                review.getRating(),
                review.getContent(),
                likeCount,
                likedByCurrentUser,
                review.getCreatedAt()
        );
    }

    private Map<Long, Long> getLikeCounts(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return Collections.emptyMap();
        }

        return reviewLikeRepository.countGroupedByReviewIds(
                        reviews.stream()
                                .map(Review::getId)
                                .collect(Collectors.toList())
                ).stream()
                .collect(Collectors.toMap(
                        ReviewLikeRepository.ReviewLikeCountProjection::getReviewId,
                        ReviewLikeRepository.ReviewLikeCountProjection::getLikeCount
                ));
    }

    private Set<Long> getLikedReviewIds(List<Review> reviews, User currentUser) {
        if (reviews.isEmpty() || currentUser == null) {
            return Collections.emptySet();
        }

        return new HashSet<>(
                reviewLikeRepository.findLikedReviewIdsByUserIdAndReviewIds(
                        currentUser.getId(),
                        reviews.stream()
                                .map(Review::getId)
                                .collect(Collectors.toList())
                )
        );
    }

    private void ensureActiveMenu(Long menuId) {
        menuRepository.findByIdAndActiveTrue(menuId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "메뉴를 찾을 수 없습니다: " + menuId));
    }
}
