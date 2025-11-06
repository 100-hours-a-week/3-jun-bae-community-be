package com.ktb.community.session;

import com.ktb.community.user.UserRole;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionProvider {

    private static final SecureRandom RNG = new SecureRandom();
    private final Map<String, Session> store = new ConcurrentHashMap<>();

    // 30분 idle 만료
    private final Duration idleTimeout = Duration.ofMinutes(30);

    public Session createSession() {
        String id = generateId();
        Session s = new Session(id);
        store.put(id, s);
        return s;
    }

    public Session getSession(String id) {
        if (id == null) return null;
        Session s = store.get(id);
        if (s == null) return null;
        if (isExpired(s)) {
            store.remove(id);
            return null;
        }
        s.touch();
        return s;
    }

    public void invalidate(String id) {
        if (id != null) store.remove(id);
    }

    public void cleanupExpired() {
        Instant now = Instant.now();
        store.values().removeIf(s -> Duration.between(s.getLastAccessAt(), now).compareTo(idleTimeout) > 0);
    }

    private boolean isExpired(Session s) {
        return Duration.between(s.getLastAccessAt(), Instant.now()).compareTo(idleTimeout) > 0;
    }

    private String generateId() {
        byte[] bytes = new byte[32];
        RNG.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
