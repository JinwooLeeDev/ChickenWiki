package com.ChickenWiki.ChickenWiki.domain.auth.dto;

public record SignupRequest(
        String username,
        String password
) {
}
