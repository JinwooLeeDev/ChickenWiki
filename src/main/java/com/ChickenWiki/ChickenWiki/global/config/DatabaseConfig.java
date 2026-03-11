package com.ChickenWiki.ChickenWiki.global.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource(Environment env) {
        // if DB_PASSWORD is not set we let Spring Boot auto-configure its default (H2)
        String password = System.getenv("DB_PASSWORD");
        if (password == null || password.isBlank()) {
            // no override, return null so the auto configuration can take over
            return null;
        }

        String url = env.getProperty("spring.datasource.url");
        String username = env.getProperty("spring.datasource.username");
        String driver = env.getProperty("spring.datasource.driver-class-name");

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        if (driver != null) ds.setDriverClassName(driver);

        return ds;
    }

}

// 환경변수를 통한 DB 비밀번호 입력이 정상적으로 입력되었는지 확인하고
// 입력되지 않았을 경우 예외를 발생시켜 애플리케이션이 실행되지 않도록 하는 컨픽파일
