package com.ChickenWiki.ChickenWiki.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserSignupRequest {
    private String username;
    private String nickname;
    private String password;
}
