package com.ChickenWiki.ChickenWiki.domain.review.repository;

import com.ChickenWiki.ChickenWiki.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByMenuId(Long menuId);
    List<Review> findByMenuIdIn(List<Long> menuIds);
}
