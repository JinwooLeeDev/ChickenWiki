package com.ChickenWiki.ChickenWiki.domain.user.service;

import com.ChickenWiki.ChickenWiki.domain.user.entity.User;
import com.ChickenWiki.ChickenWiki.domain.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserSessionService {

    private final Map<String, Long> sessions = new ConcurrentHashMap<>();
    private final UserRepository userRepository;

    public UserSessionService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String createSession(User user) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, user.getId());
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
        Long userId = sessions.get(token);

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 세션이 유효하지 않습니다.");
        }

        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            sessions.remove(token);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 세션이 유효하지 않습니다.");
        }

        return user;
    }

    public User findUserByAuthorizationHeaderOrNull(String authorizationHeader) {
        try {
            return getUserByAuthorizationHeader(authorizationHeader);
        } catch (ResponseStatusException e) {
            return null;
        }
    }

    public void invalidateSessionsForUser(Long userId) {
        sessions.entrySet().removeIf(entry -> entry.getValue().equals(userId));
    }
}
