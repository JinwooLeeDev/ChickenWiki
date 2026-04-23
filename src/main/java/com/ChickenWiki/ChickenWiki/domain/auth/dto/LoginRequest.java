package com.ChickenWiki.ChickenWiki.domain.auth.dto;

public record LoginRequest(
        String username,
        String password
) {
}
