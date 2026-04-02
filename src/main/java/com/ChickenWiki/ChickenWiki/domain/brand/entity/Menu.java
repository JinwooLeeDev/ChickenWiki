package com.ChickenWiki.ChickenWiki.domain.brand.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "menus")
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_menu_id")
    private Long sourceMenuId;

    @Column(name = "menu_name", nullable = false)
    private String menuName;

    @Column(name = "menu_price")
    private Integer menuPrice;

    @Column(name = "menu_image_url")
    private String menuImageUrl;

    @Column(name = "description")
    private String description;

    @Column(name = "brand_name", nullable = false)
    private String brandName;

    @Column(name = "crawled_at", nullable = false)
    private LocalDateTime crawledAt;

    public Menu(Long sourceMenuId,
                String menuName,
                Integer menuPrice,
                String menuImageUrl,
                String description,
                String brandName) {
        this.sourceMenuId = sourceMenuId;
        this.menuName = menuName;
        this.menuPrice = menuPrice;
        this.menuImageUrl = menuImageUrl;
        this.description = description;
        this.brandName = brandName;
        this.crawledAt = LocalDateTime.now();
    }

    public Menu(String menuName,
                Integer menuPrice,
                String menuImageUrl,
                String description,
                String brandName) {
        this.sourceMenuId = null;
        this.menuName = menuName;
        this.menuPrice = menuPrice;
        this.menuImageUrl = menuImageUrl;
        this.description = description;
        this.brandName = brandName;
        this.crawledAt = LocalDateTime.now();
    }

    public void updateMenuInfo(String menuName,
                               Integer menuPrice,
                               String menuImageUrl,
                               String description) {
        this.menuName = menuName;
        this.menuPrice = menuPrice;
        this.menuImageUrl = menuImageUrl;
        this.description = description;
        this.crawledAt = LocalDateTime.now();
    }

    public boolean hasSameContent(String menuName,
                                  Integer menuPrice,
                                  String menuImageUrl,
                                  String description) {
        return Objects.equals(this.menuName, menuName)
                && Objects.equals(this.menuPrice, menuPrice)
                && Objects.equals(this.menuImageUrl, menuImageUrl)
                && Objects.equals(this.description, description);
    }
}
