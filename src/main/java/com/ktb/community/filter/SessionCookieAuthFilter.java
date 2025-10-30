package com.ktb.community.filter;

import com.ktb.community.session.Session;
import com.ktb.community.session.SessionProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SessionCookieAuthFilter extends OncePerRequestFilter {
    private final SessionProvider sessionProvider;
    private List<String> excludePathPatterns = new ArrayList<>(List.of("/api/auth", "/swagger-ui", "/v3/api-docs"));

    // 필러 제외 로직
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true; // CORS 프리플라이트 통과
        String path = request.getRequestURI();
        return Arrays.stream(excludePathPatterns.toArray(new String[0])).anyMatch(path::startsWith);
    }

    // 필터 적용 로직
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws IOException, ServletException {
        Optional<String> token = extractToken(request);
        // 예외 경로인 경우 필터 바로 통과
        if (shouldNotFilter(request)) {
            chain.doFilter(request, response);
            return;
        }
        // 토큰 없음 → index 요청 시 login으로 리다이렉트
        if (token.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "UnAuthorized");
            return;
        }

        // 토큰 검증 및 속성 설정
        if (!validateAndSetAttributes(token.get(), request)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "UnAuthorized (Invalid Token)");
            return;
        }

        // 토큰 검증 성공 시 통과
        chain.doFilter(request, response);
    }

    // request에서 session cookie token 추출
    private Optional<String> extractToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getCookies())
                .stream()
                .flatMap(Arrays::stream)
                .filter(cookie -> "JSESSIONID".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    private boolean validateAndSetAttributes(String token, HttpServletRequest request) {
        try {
            Session session = sessionProvider.getSession(token);
            if (session == null) {
                return false;
            }
            request.setAttribute("userId", session.getAttr("userId"));
            request.setAttribute("roles", session.getAttr("roles"));
            return true;
        } catch (Exception exception) {
            return false;
        }
    }
}
