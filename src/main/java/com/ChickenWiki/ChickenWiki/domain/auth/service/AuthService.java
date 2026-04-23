package com.ChickenWiki.ChickenWiki.domain.auth.service;

import com.ChickenWiki.ChickenWiki.domain.auth.dto.AuthResponse;
import com.ChickenWiki.ChickenWiki.domain.auth.dto.LoginRequest;
import com.ChickenWiki.ChickenWiki.domain.auth.dto.MeResponse;
import com.ChickenWiki.ChickenWiki.domain.auth.dto.SignupRequest;
import com.ChickenWiki.ChickenWiki.domain.user.entity.User;
import com.ChickenWiki.ChickenWiki.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final String ROLE_USER = "USER";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (request == null) {
            throw badRequest("회원가입 정보를 입력해주세요.");
        }

        String username = normalizeUsername(request.username());
        String password = normalizePassword(request.password());

        if (username.length() < 2 || username.length() > 30) {
            throw badRequest("아이디는 2자 이상 30자 이하로 입력해주세요.");
        }
        if (password.length() < 4 || password.length() > 100) {
            throw badRequest("비밀번호는 4자 이상 100자 이하로 입력해주세요.");
        }
        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다.");
        }

        User user = userRepository.save(new User(username, username, passwordEncoder.encode(password), ROLE_USER));
        return createAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        if (request == null) {
            throw badRequest("로그인 정보를 입력해주세요.");
        }

        String username = normalizeUsername(request.username());
        String password = normalizePassword(request.password());

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> unauthorized("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!matchesPassword(password, user)) {
            throw unauthorized("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        if (isLegacyPlainPassword(user.getPassword())) {
            user.changePassword(passwordEncoder.encode(password));
        }

        return createAuthResponse(user);
    }

    public MeResponse me(String authorizationHeader) {
        TokenService.TokenClaims claims = tokenService.parseToken(extractBearerToken(authorizationHeader));
        return new MeResponse(claims.userId(), claims.username(), claims.role());
    }

    private AuthResponse createAuthResponse(User user) {
        return new AuthResponse(
                tokenService.createToken(user),
                user.getId(),
                user.getUsername(),
                user.getRole()
        );
    }

    private boolean matchesPassword(String rawPassword, User user) {
        String savedPassword = user.getPassword();
        if (savedPassword == null || savedPassword.isBlank()) {
            return false;
        }
        if (isLegacyPlainPassword(savedPassword)) {
            return savedPassword.equals(rawPassword);
        }
        return passwordEncoder.matches(rawPassword, savedPassword);
    }

    private boolean isLegacyPlainPassword(String savedPassword) {
        return !(savedPassword.startsWith("$2a$")
                || savedPassword.startsWith("$2b$")
                || savedPassword.startsWith("$2y$"));
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw unauthorized("로그인이 필요합니다.");
        }
        String token = authorizationHeader.substring("Bearer ".length()).trim();
        if (token.isBlank()) {
            throw unauthorized("로그인이 필요합니다.");
        }
        return token;
    }

    private String normalizeUsername(String username) {
        if (username == null) {
            throw badRequest("아이디를 입력해주세요.");
        }
        String normalized = username.trim();
        if (normalized.isBlank()) {
            throw badRequest("아이디를 입력해주세요.");
        }
        return normalized;
    }

    private String normalizePassword(String password) {
        if (password == null || password.isBlank()) {
            throw badRequest("비밀번호를 입력해주세요.");
        }
        return password;
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException unauthorized(String message) {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, message);
    }
}
