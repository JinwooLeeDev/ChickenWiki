package com.ChickenWiki.ChickenWiki.domain.menu.service;

import com.ChickenWiki.ChickenWiki.domain.brand.dto.MenuDto;
import com.ChickenWiki.ChickenWiki.domain.brand.entity.Menu;
import com.ChickenWiki.ChickenWiki.domain.brand.repository.MenuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;

    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    public List<MenuDto> findAllMenus() {
        return menuRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public MenuDto findMenuById(Long menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴가 없습니다: " + menuId));
        return toDto(menu);
    }

    private MenuDto toDto(Menu m) {
        return new MenuDto(m.getId(), m.getMenuName(), m.getMenuPrice(),
                m.getMenuImageUrl(), m.getDescription(), m.getBrandName(), m.getCrawledAt());
    }
}
