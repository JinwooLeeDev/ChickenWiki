package com.ChickenWiki.ChickenWiki.domain.review.repository;

import com.ChickenWiki.ChickenWiki.domain.review.entity.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    long countByReviewId(Long reviewId);

    boolean existsByUserIdAndReviewId(Long userId, Long reviewId);

    void deleteByReviewId(Long reviewId);

    void deleteByReviewIdIn(Collection<Long> reviewIds);

    void deleteByUserId(Long userId);

    @Query("""
            select rl.reviewId as reviewId, count(rl.id) as likeCount
            from ReviewLike rl
            where rl.reviewId in :reviewIds
            group by rl.reviewId
            """)
    List<ReviewLikeCountProjection> countGroupedByReviewIds(@Param("reviewIds") Collection<Long> reviewIds);

    @Query("""
            select rl.reviewId
            from ReviewLike rl
            where rl.userId = :userId
              and rl.reviewId in :reviewIds
            """)
    List<Long> findLikedReviewIdsByUserIdAndReviewIds(@Param("userId") Long userId, @Param("reviewIds") Collection<Long> reviewIds);

    interface ReviewLikeCountProjection {
        Long getReviewId();

        Long getLikeCount();
    }
}
