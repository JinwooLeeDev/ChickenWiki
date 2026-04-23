package com.ChickenWiki.ChickenWiki.domain.user.service;

import com.ChickenWiki.ChickenWiki.domain.brand.entity.Menu;
import com.ChickenWiki.ChickenWiki.domain.brand.repository.MenuRepository;
import com.ChickenWiki.ChickenWiki.domain.review.entity.Review;
import com.ChickenWiki.ChickenWiki.domain.review.repository.ReviewRepository;
import com.ChickenWiki.ChickenWiki.domain.user.dto.UserAuthResponse;
import com.ChickenWiki.ChickenWiki.domain.user.dto.UserLoginRequest;
import com.ChickenWiki.ChickenWiki.domain.user.dto.UserMyPageResponse;
import com.ChickenWiki.ChickenWiki.domain.user.dto.UserReviewSummaryResponse;
import com.ChickenWiki.ChickenWiki.domain.user.dto.UserSignupRequest;
import com.ChickenWiki.ChickenWiki.domain.user.entity.User;
import com.ChickenWiki.ChickenWiki.domain.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserSessionService userSessionService;
    private final ReviewRepository reviewRepository;
    private final MenuRepository menuRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository,
                       UserSessionService userSessionService,
                       ReviewRepository reviewRepository,
                       MenuRepository menuRepository) {
        this.userRepository = userRepository;
        this.userSessionService = userSessionService;
        this.reviewRepository = reviewRepository;
        this.menuRepository = menuRepository;
    }

    public UserAuthResponse signup(UserSignupRequest request) {
        String username = normalizeUsername(request.getUsername());
        String nickname = normalizeNickname(request.getNickname());
        String password = normalizePassword(request.getPassword());

        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다.");
        }

        if (userRepository.existsByNickname(nickname)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다.");
        }

        User savedUser = userRepository.save(new User(username, nickname, passwordEncoder.encode(password), "USER"));
        return toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserAuthResponse login(UserLoginRequest request) {
        String username = normalizeUsername(request.getUsername());
        String password = normalizePassword(request.getPassword());

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "아이디 또는 비밀번호가 올바르지 않습니다."
                ));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "아이디 또는 비밀번호가 올바르지 않습니다."
            );
        }

        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserMyPageResponse getMyPage(User user) {
        List<Review> reviews = reviewRepository.findByAuthorOrderByCreatedAtDesc(user.getNickname());

        Map<Long, Menu> menuById = menuRepository.findAllById(
                        reviews.stream()
                                .map(Review::getMenuId)
                                .distinct()
                                .collect(Collectors.toList())
                ).stream()
                .collect(Collectors.toMap(Menu::getId, Function.identity()));

        List<UserReviewSummaryResponse> reviewResponses = reviews.stream()
                .map(review -> {
                    Menu menu = menuById.get(review.getMenuId());
                    return new UserReviewSummaryResponse(
                            review.getId(),
                            review.getMenuId(),
                            menu != null ? menu.getMenuName() : "삭제되었거나 없는 메뉴",
                            menu != null ? menu.getBrandName() : "",
                            menu != null ? menu.getMenuImageUrl() : null,
                            review.getRating(),
                            review.getContent(),
                            review.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());

        return new UserMyPageResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getRole(),
                user.getCreatedAt(),
                reviewResponses
        );
    }

    private UserAuthResponse toResponse(User user) {
        String token = userSessionService.createSession(user);

        return new UserAuthResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getRole(),
                token,
                user.getCreatedAt()
        );
    }

    private String normalizeUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "아이디를 입력해주세요.");
        }

        String normalized = username.trim();
        if (normalized.length() < 4 || normalized.length() > 30) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "아이디는 4자 이상 30자 이하로 입력해주세요."
            );
        }

        if (!normalized.matches("[A-Za-z0-9._-]+")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "아이디는 영문, 숫자, ., _, - 만 사용할 수 있습니다."
            );
        }

        return normalized;
    }

    private String normalizeNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "닉네임을 입력해주세요.");
        }

        String normalized = nickname.trim();
        if (normalized.length() < 2 || normalized.length() > 20) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "닉네임은 2자 이상 20자 이하로 입력해주세요."
            );
        }

        return normalized;
    }

    private String normalizePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호를 입력해주세요.");
        }

        String normalized = password.trim();
        if (normalized.length() < 4 || normalized.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "비밀번호는 4자 이상으로 입력해주세요."
            );
        }

        return normalized;
    }
}
