package com.ChickenWiki.ChickenWiki.domain.brand.repository;

import com.ChickenWiki.ChickenWiki.domain.brand.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    void deleteByBrandName(String brandName);
    Optional<Menu> findByBrandNameAndSourceMenuId(String brandName, Long sourceMenuId);
    List<Menu> findByBrandName(String brandName);
    List<Menu> findByBrandNameAndActiveTrue(String brandName);
    List<Menu> findAllByActiveTrue();
    Optional<Menu> findByIdAndActiveTrue(Long id);

    @Query("select max(m.crawledAt) from Menu m where m.brandName = :brandName")
    Optional<LocalDateTime> findLatestCrawledAtByBrandName(@Param("brandName") String brandName);
}
