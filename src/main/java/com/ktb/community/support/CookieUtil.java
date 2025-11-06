package com.ktb.community.support;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

public class CookieUtil {
    public static ResponseCookie makeSessionCookie(String sessionId, boolean secure) {
        return ResponseCookie.from("JSESSIONID", sessionId)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .sameSite("Lax")
                .maxAge(24 * 60 * 60) //1d
                .build();
    }

    public static ResponseCookie deleteSessionCookie(boolean secure) {
        return org.springframework.http.ResponseCookie.from("JSESSIONID", "")
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ZERO)
                .build();
    }

}
