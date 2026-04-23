package com.ChickenWiki.ChickenWiki.domain.user.service;

import com.ChickenWiki.ChickenWiki.domain.user.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserSessionService {

    private final Map<String, User> sessions = new ConcurrentHashMap<>();

    public String createSession(User user) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, user);
        return token;
    }

    public User getUserByAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        if (!authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 토큰 형식이 올바르지 않습니다.");
        }

        String token = authorizationHeader.substring(7).trim();
        User user = sessions.get(token);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 세션이 유효하지 않습니다.");
        }

        return user;
    }
}
