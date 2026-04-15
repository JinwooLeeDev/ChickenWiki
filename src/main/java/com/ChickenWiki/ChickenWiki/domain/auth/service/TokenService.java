package com.ChickenWiki.ChickenWiki.domain.auth.service;

import com.ChickenWiki.ChickenWiki.domain.user.entity.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class TokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;
    private final byte[] secret;
    private final long tokenValidHours;

    public TokenService(
            ObjectMapper objectMapper,
            @Value("${app.auth.jwt-secret}") String jwtSecret,
            @Value("${app.auth.token-valid-hours}") long tokenValidHours
    ) {
        this.objectMapper = objectMapper;
        this.secret = jwtSecret.getBytes(StandardCharsets.UTF_8);
        this.tokenValidHours = tokenValidHours;
    }

    public String createToken(User user) {
        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", user.getId());
        payload.put("username", user.getUsername());
        payload.put("role", user.getRole());
        payload.put("exp", Instant.now().plusSeconds(tokenValidHours * 3600).getEpochSecond());

        String unsignedToken = encodeJson(header) + "." + encodeJson(payload);
        return unsignedToken + "." + sign(unsignedToken);
    }

    public TokenClaims parseToken(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw unauthorized("올바르지 않은 로그인 토큰입니다.");
        }

        String unsignedToken = parts[0] + "." + parts[1];
        String expectedSignature = sign(unsignedToken);
        if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
            throw unauthorized("로그인 토큰 검증에 실패했습니다.");
        }

        Map<String, Object> payload = decodeJson(parts[1]);
        Number exp = (Number) payload.get("exp");
        if (exp == null || Instant.now().getEpochSecond() > exp.longValue()) {
            throw unauthorized("로그인 시간이 만료되었습니다. 다시 로그인해주세요.");
        }

        Number userId = (Number) payload.get("sub");
        String username = (String) payload.get("username");
        String role = (String) payload.get("role");
        if (userId == null || username == null || role == null) {
            throw unauthorized("로그인 토큰 정보가 부족합니다.");
        }

        return new TokenClaims(userId.longValue(), username, role);
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            byte[] json = objectMapper.writeValueAsBytes(value);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
        } catch (Exception e) {
            throw new IllegalStateException("토큰 생성에 실패했습니다.", e);
        }
    }

    private Map<String, Object> decodeJson(String value) {
        try {
            byte[] json = Base64.getUrlDecoder().decode(value);
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (Exception e) {
            throw unauthorized("로그인 토큰을 읽을 수 없습니다.");
        }
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("토큰 서명에 실패했습니다.", e);
        }
    }

    private ResponseStatusException unauthorized(String message) {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, message);
    }

    public record TokenClaims(
            Long userId,
            String username,
            String role
    ) {
    }
}
