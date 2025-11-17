package com.ktb.community.file;

import com.ktb.community.entity.File;
import com.ktb.community.repository.FileRepository;
import com.ktb.community.service.FileStorageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FileCleaner {
    private static final Duration ORPHAN_RETENTION = Duration.ofHours(1);

    private final FileStorageService fileStorageService;
    private final FileRepository fileRepository;
    // 매 시간 고아 파일 cleanup
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanUpOrphanedFiles() {
        Instant expiration = Instant.now().minus(ORPHAN_RETENTION);
        List<File> orphans = fileRepository.findByCommittedFalseAndDeletedAtIsNullAndCreatedAtBefore(expiration);
        if (orphans.isEmpty()) {
            return;
        }
        orphans.forEach(file -> {
            fileStorageService.delete(file.getStorageKey());
            file.markDeleted();
        });
    }

}
