package com.ktb.community.service;

import com.ktb.community.dto.auth.TokenResponse;
import com.ktb.community.entity.RefreshToken;
import com.ktb.community.entity.User;
import com.ktb.community.provider.JwtProvider;
import com.ktb.community.repository.RefreshTokenRepository;
import com.ktb.community.repository.UserRepository;
import com.ktb.community.session.Session;
import com.ktb.community.support.TokenUtil;
import com.ktb.community.user.UserRole;
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
    public TokenResponse userLogin(String email, String password) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid username or password");
        }

        userService.updateLastLogin(user);
//        List<String> roles = new ArrayList<>();
//        roles.add(UserRole.USER.name());
//
//        if (user.isAdmin()){
//            roles.add(UserRole.ADMIN.name());
//        }

        // 기존 리프레시 토큰 무효화 (이러면 동시 로그인 못함)
        refreshTokenRepository.deleteByUserId(user.getId());

        // 새로운 토큰 발급 및 저장
        TokenResponse tokenResponse = generateAndSaveTokens(user);

        // 쿠키 추가 이걸 왜 서비스에서? -> 이걸 컨트롤러로 빼는게 차라리 더 결합도가 낮고 더 안전함.
        //addTokenCookies(response, tokenResponse);

        return tokenResponse;
    }

    /** Access / Refresh 토큰을 새로 발급하고 DB에 저장 */
    private TokenResponse generateAndSaveTokens(User user) {
        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail());
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
    public TokenResponse refreshAccessToken(String refreshToken) {
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

        // refresh token은 유지하고 access token만 새로 발급
        String newAccessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail());

        // access token 쿠키만 갱신 -> 컨트롤러에서 처리
        // addTokenCookie(response, "accessToken", newAccessToken, ACCESS_TOKEN_EXPIRATION);

        return new TokenResponse(newAccessToken, refreshToken);

    }

}
