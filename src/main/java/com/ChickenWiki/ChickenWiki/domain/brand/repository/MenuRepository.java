package com.ChickenWiki.ChickenWiki.domain.brand.repository;

import com.ChickenWiki.ChickenWiki.domain.brand.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    void deleteByBrandName(String brandName);
    Optional<Menu> findByBrandNameAndSourceMenuId(String brandName, Long sourceMenuId);
    List<Menu> findByBrandName(String brandName);
}