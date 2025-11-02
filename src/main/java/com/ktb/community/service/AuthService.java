package com.ktb.community.service;

import com.ktb.community.dto.auth.AuthResponse;
import com.ktb.community.dto.auth.TokenResponse;
import com.ktb.community.entity.RefreshToken;
import com.ktb.community.entity.User;
import com.ktb.community.provider.JwtProvider;
import com.ktb.community.repository.RefreshTokenRepository;
import com.ktb.community.repository.UserRepository;
import com.ktb.community.session.Session;
import com.ktb.community.support.TokenUtil;
import com.ktb.community.user.UserRole;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.ktb.community.support.TokenUtil.*;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @Transactional
    public AuthResponse userLogin(String email, String password, HttpServletResponse response) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid username or password");
        }

        userService.updateLastLogin(user);
        List<String> roles = new ArrayList<>();
        roles.add(UserRole.USER.name());

        if (user.isAdmin()){
            roles.add(UserRole.ADMIN.name());
        }

        // 기존 리프레시 토큰 무효화 (이러면 동시 로그인 못함)
        refreshTokenRepository.deleteByUserId(user.getId());

        // 새로운 토큰 발급 및 저장
        TokenResponse tokenResponse = generateAndSaveTokens(user);

        // refresh token을 쿠키에 추가, 엑세스 토큰은 바디로 전송
        addTokenCookie(response, "refreshToken", tokenResponse.refreshToken(), REFRESH_TOKEN_EXPIRATION);

        return AuthResponse.from(user, roles, tokenResponse);
    }

    public void logoutUser(HttpServletResponse response) {
        // refreshToken 쿠키 즉시 만료
        addTokenCookie(response, "refreshToken", null, 0);
    }

    /** Access / Refresh 토큰을 새로 발급하고 DB에 저장 */
    private TokenResponse generateAndSaveTokens(User user) {
        String role = UserRole.USER.name();
        if (user.isAdmin()) {
            role = UserRole.ADMIN.name();
        }
        String accessToken = jwtProvider.createAccessToken(user.getId(), role);
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        RefreshToken refreshEntity = new RefreshToken();
        refreshEntity.setUserId(user.getId());
        refreshEntity.setToken(refreshToken);
        refreshEntity.setExpiresAt(Instant.now().plusSeconds(REFRESH_TOKEN_EXPIRATION));
        refreshEntity.setRevoked(false);
        refreshTokenRepository.save(refreshEntity);

        return new TokenResponse(accessToken, refreshToken);
    }


    @Transactional
    public TokenResponse refreshAccessToken(String refreshToken, HttpServletResponse response) {
        var parsedRefreshToken = jwtProvider.parse(refreshToken);

        RefreshToken entity = refreshTokenRepository.findByTokenAndRevokedFalse(refreshToken).orElse(null);

        if (entity == null || entity.getExpiresAt().isBefore(Instant.now())) {
            return null;
        }

        Long userId = Long.valueOf(parsedRefreshToken.getBody().getSubject());
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return null;
        }

        String role = UserRole.USER.name();
        if (user.isAdmin()) {
            role = UserRole.ADMIN.name();
        }
        // refresh token은 유지하고 access token만 새로 발급
        String newAccessToken = jwtProvider.createAccessToken(user.getId(), role);

        // access token 리턴
        return new TokenResponse(newAccessToken, refreshToken);

    }

    /** 공통 쿠키 생성 로직 */
    private void addTokenCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }
}
