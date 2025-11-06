package com.ktb.community.session;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SessionCleaner {
    private final SessionProvider store;

    // 1분마다 만료 세션 삭제
    @Scheduled(fixedDelay = 60_000)
    public void run() { store.cleanupExpired(); }
}
