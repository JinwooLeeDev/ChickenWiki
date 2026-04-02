package com.ChickenWiki.ChickenWiki.domain.brand.repository;

import com.ChickenWiki.ChickenWiki.domain.brand.entity.MenuTagMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuTagMappingRepository extends JpaRepository<MenuTagMapping, Long> {

    void deleteByMenuIdAndTagTagType(Long menuId, com.ChickenWiki.ChickenWiki.domain.brand.entity.TagType tagType);
    boolean existsByMenuIdAndTagId(Long menuId, Long tagId);
}
