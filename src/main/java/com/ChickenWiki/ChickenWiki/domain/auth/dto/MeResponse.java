package com.ChickenWiki.ChickenWiki.domain.auth.dto;

public record MeResponse(
        Long userId,
        String username,
        String role
) {
}
