package com.ChickenWiki.ChickenWiki.domain.brand.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "menus")
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String menuName;
    private Integer menuPrice;
    private String menuImageUrl;
    private String description;
    private String brandName;
    private LocalDateTime crawledAt;

    public Menu(String menuName, Integer menuPrice, String menuImageUrl,
                String description, String brandName) {
        this.menuName = menuName;
        this.menuPrice = menuPrice;
        this.menuImageUrl = menuImageUrl;
        this.description = description;
        this.brandName = brandName;
        this.crawledAt = LocalDateTime.now();
    }
}