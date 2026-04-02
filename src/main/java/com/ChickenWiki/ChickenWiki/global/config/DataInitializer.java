package com.ChickenWiki.ChickenWiki.global.config;

import com.ChickenWiki.ChickenWiki.domain.brand.entity.Brand;
import com.ChickenWiki.ChickenWiki.domain.brand.entity.Menu;
import com.ChickenWiki.ChickenWiki.domain.brand.repository.BrandRepository;
import com.ChickenWiki.ChickenWiki.domain.brand.repository.MenuRepository;
import com.ChickenWiki.ChickenWiki.domain.review.entity.Review;
import com.ChickenWiki.ChickenWiki.domain.review.repository.ReviewRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 간단한 샘플 데이터를 초기화합니다. Supabase 연결이 준비되어 있다면 해당 데이터베이스에 저장됩니다.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final BrandRepository brandRepository;
    private final MenuRepository menuRepository;
    private final ReviewRepository reviewRepository;

    public DataInitializer(BrandRepository brandRepository,
                           MenuRepository menuRepository,
                           ReviewRepository reviewRepository) {
        this.brandRepository = brandRepository;
        this.menuRepository = menuRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (brandRepository.count() > 0) {
            // 이미 데이터가 있는 경우 초기화를 건너뛴다.
            return;
        }
    }
}
