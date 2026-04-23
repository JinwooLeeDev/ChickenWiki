package com.ChickenWiki.ChickenWiki.global.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource(Environment env) {
        String url = env.getProperty("spring.datasource.url");
        String username = env.getProperty("spring.datasource.username");
        String password = env.getProperty("spring.datasource.password");
        if (password == null || password.isBlank()) {
            password = env.getProperty("DB_PASSWORD");
        }
        String driver = env.getProperty("spring.datasource.driver-class-name");

        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "Database password is missing. " +
                    "Set spring.datasource.password or DB_PASSWORD before starting the backend so ChickenWiki can connect to Supabase."
            );
        }

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        if (driver != null && !driver.isBlank()) {
            dataSource.setDriverClassName(driver);
        }

        return dataSource;
    }
}
