package com.ChickenWiki.ChickenWiki.domain.brand.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "tag_type", nullable = false)
    private TagType tagType;

    @Column(name = "brand_name", nullable = false)
    private String brandName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Tag(String name, TagType tagType, String brandName) {
        this.name = name;
        this.tagType = tagType;
        this.brandName = brandName;
        this.createdAt = LocalDateTime.now();
    }
}