package com.ChickenWiki.ChickenWiki.domain.review.repository;

import com.ChickenWiki.ChickenWiki.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByMenuIdOrderByCreatedAtDesc(Long menuId);
    List<Review> findByMenuIdIn(List<Long> menuIds);
    List<Review> findByAuthorOrderByCreatedAtDesc(String author);

    @Query("""
            select r.menuId as menuId,
                   count(r.id) as reviewCount,
                   avg(r.rating) as averageRating
            from Review r
            where r.menuId in :menuIds
            group by r.menuId
            """)
    List<MenuReviewStatsProjection> findMenuReviewStatsByMenuIds(@Param("menuIds") Collection<Long> menuIds);

    interface MenuReviewStatsProjection {
        Long getMenuId();
        Long getReviewCount();
        Double getAverageRating();
    }
}
