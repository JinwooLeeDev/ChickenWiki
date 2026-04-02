package com.ChickenWiki.ChickenWiki.domain.brand.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "menu_tag_mappings")
public class MenuTagMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @Column(name = "source_menu_id")
    private Long sourceMenuId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public MenuTagMapping(Menu menu, Tag tag) {
        this.menu = menu;
        this.tag = tag;
        this.sourceMenuId = menu.getSourceMenuId();
        this.createdAt = LocalDateTime.now();
    }
}
