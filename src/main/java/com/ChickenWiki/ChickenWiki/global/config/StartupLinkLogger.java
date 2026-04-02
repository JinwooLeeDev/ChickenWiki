package com.ChickenWiki.ChickenWiki.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupLinkLogger {

    private final Environment environment;

    @EventListener(ApplicationReadyEvent.class)
    public void logLinks() {
        String port = environment.getProperty("local.server.port",
                environment.getProperty("server.port", "8080"));
        String host = environment.getProperty("app.local-host", "localhost");
        String backendBaseUrl = "http://" + host + ":" + port;
        String frontendUrl = environment.getProperty("app.frontend.url", "").trim();

        log.info("========================================");
        log.info("ChickenWiki local links");
        if (!frontendUrl.isBlank()) {
            log.info("Frontend : {}", frontendUrl);
        }
        log.info("API root : {}", backendBaseUrl);
        log.info("Crawl all: {}/api/crawl/all", backendBaseUrl);
        log.info("Brands   : {}/api/brands", backendBaseUrl);
        log.info("Menus    : {}/api/menus", backendBaseUrl);
        log.info("========================================");
    }
}
