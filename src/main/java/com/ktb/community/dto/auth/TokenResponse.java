package com.ktb.community.dto.auth;

public record TokenResponse (String accessToken, String refreshToken) {
}
