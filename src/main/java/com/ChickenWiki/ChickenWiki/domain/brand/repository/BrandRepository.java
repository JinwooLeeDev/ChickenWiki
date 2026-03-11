package com.ChickenWiki.ChickenWiki.domain.brand.repository;

import com.ChickenWiki.ChickenWiki.domain.brand.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {
}
