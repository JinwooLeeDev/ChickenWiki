package com.ChickenWiki.ChickenWiki.domain.review.service;

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

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public List<ReviewDto> findByMenuId(Long menuId) {
        return reviewRepository.findByMenuIdOrderByCreatedAtDesc(menuId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ReviewDto create(Long menuId, ReviewCreateRequest request) {
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
}
