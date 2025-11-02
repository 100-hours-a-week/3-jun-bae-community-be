package com.ktb.community.support;

import com.ktb.community.entity.RefreshToken;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

public class TokenUtil {
    public static final int ACCESS_TOKEN_EXPIRATION = 15 * 60; // 15분
    public static final int REFRESH_TOKEN_EXPIRATION = 14 * 24 * 3600; // 14일


    /** 공통 쿠키 생성 로직 */
    public static ResponseCookie makeTokenCookie(String name, String value, int maxAge, boolean secure) {
        return org.springframework.http.ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .sameSite("Lax")
                .maxAge(maxAge)
                .build();
    }

    public static ResponseCookie deleteTokenCookie(String name, boolean secure) {
        return org.springframework.http.ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ZERO)
                .build();
    }
}
