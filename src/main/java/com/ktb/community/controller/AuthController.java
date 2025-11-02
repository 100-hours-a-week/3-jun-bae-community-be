package com.ktb.community.controller;

import com.ktb.community.dto.auth.AuthResponse;
import com.ktb.community.dto.auth.LoginRequest;
import com.ktb.community.dto.auth.TokenResponse;
import com.ktb.community.service.AuthService;
import com.ktb.community.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static com.ktb.community.support.TokenUtil.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            AuthResponse authResponse = authService.userLogin(request.email(), request.password(), response);
            return ResponseEntity.ok()
                    .body(authResponse);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(@RequestAttribute Long userId,  HttpServletResponse response) {
        if (userId != null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized");
        }
        authService.logoutUser(response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@CookieValue(value = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {
        if (refreshToken == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Refresh token invalid or expired");
        }

        try {
            TokenResponse tokenRes = authService.refreshAccessToken(refreshToken, response);

            if (tokenRes == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Refresh token invalid or expired");
            }

            return ResponseEntity.ok().body(tokenRes);

        } catch (ResponseStatusException exception) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Refresh token invalid or expired");
        }
    }

    private Optional<String> extractRefreshTokenFromCookie(HttpServletRequest request) {
        return Optional.ofNullable(request.getCookies())
                .stream()
                .flatMap(Arrays::stream)
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}
