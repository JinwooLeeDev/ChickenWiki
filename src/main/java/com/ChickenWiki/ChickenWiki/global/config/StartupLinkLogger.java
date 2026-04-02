package com.ChickenWiki.ChickenWiki.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

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

        System.out.println();
        System.out.println("========================================");
        System.out.println("ChickenWiki local links");
        if (!frontendUrl.isBlank()) {
            System.out.println("Frontend : " + frontendUrl);
        }
        System.out.println("API root : " + backendBaseUrl);
        System.out.println("Crawl all: " + backendBaseUrl + "/api/crawl/all");
        System.out.println("Brands   : " + backendBaseUrl + "/api/brands");
        System.out.println("Menus    : " + backendBaseUrl + "/api/menus");
        System.out.println("========================================");
        System.out.println();
    }
}
