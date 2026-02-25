package com.ChickenWiki.ChickenWiki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ChickenWikiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChickenWikiApplication.class, args);
	}

}