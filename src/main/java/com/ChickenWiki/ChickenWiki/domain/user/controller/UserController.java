package com.ChickenWiki.ChickenWiki.domain.user.controller;

import com.ChickenWiki.ChickenWiki.domain.user.dto.UserAuthResponse;
import com.ChickenWiki.ChickenWiki.domain.user.dto.UserLoginRequest;
import com.ChickenWiki.ChickenWiki.domain.user.dto.UserMyPageResponse;
import com.ChickenWiki.ChickenWiki.domain.user.dto.UserSignupRequest;
import com.ChickenWiki.ChickenWiki.domain.user.entity.User;
import com.ChickenWiki.ChickenWiki.domain.user.service.UserSessionService;
import com.ChickenWiki.ChickenWiki.domain.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserSessionService userSessionService;

    public UserController(UserService userService, UserSessionService userSessionService) {
        this.userService = userService;
        this.userSessionService = userSessionService;
    }

    @PostMapping("/signup")
    public UserAuthResponse signup(@RequestBody UserSignupRequest request) {
        return userService.signup(request);
    }

    @PostMapping("/login")
    public UserAuthResponse login(@RequestBody UserLoginRequest request) {
        return userService.login(request);
    }

    @GetMapping("/me")
    public UserMyPageResponse me(@RequestHeader("Authorization") String authorizationHeader) {
        User currentUser = userSessionService.getUserByAuthorizationHeader(authorizationHeader);
        return userService.getMyPage(currentUser);
    }
}
