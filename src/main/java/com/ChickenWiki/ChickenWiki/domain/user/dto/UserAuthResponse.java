package com.ChickenWiki.ChickenWiki.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserAuthResponse {
    private Long id;
    private String username;
    private String nickname;
    private String role;
    private String token;
    private LocalDateTime createdAt;
}
