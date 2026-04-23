package com.ChickenWiki.ChickenWiki.domain.auth.dto;

public record AuthResponse(
        String accessToken,
        Long userId,
        String username,
        String role
) {
}
