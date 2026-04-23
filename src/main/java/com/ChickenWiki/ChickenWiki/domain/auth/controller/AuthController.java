package com.ChickenWiki.ChickenWiki.domain.auth.controller;

import com.ChickenWiki.ChickenWiki.domain.auth.dto.AuthResponse;
import com.ChickenWiki.ChickenWiki.domain.auth.dto.LoginRequest;
import com.ChickenWiki.ChickenWiki.domain.auth.dto.MeResponse;
import com.ChickenWiki.ChickenWiki.domain.auth.dto.SignupRequest;
import com.ChickenWiki.ChickenWiki.domain.auth.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public AuthResponse signup(@RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public MeResponse me(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        return authService.me(authorizationHeader);
    }
}
