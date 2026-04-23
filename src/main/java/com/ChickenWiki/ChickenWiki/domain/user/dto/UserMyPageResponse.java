package com.ChickenWiki.ChickenWiki.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class UserMyPageResponse {
    private Long id;
    private String username;
    private String nickname;
    private String role;
    private LocalDateTime createdAt;
    private List<UserReviewSummaryResponse> reviews;
}
