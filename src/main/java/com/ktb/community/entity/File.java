package com.ktb.community.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String storageKey;

    @Column(nullable = false)
    private String fileUrl;

    @Column(nullable = false)
    private int fileSize;

    @Column(name = "is_committed")
    private boolean committed;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    private Instant deletedAt;

    public static File pending(String originalFileName, String storageKey, String fileUrl, int fileSize) {
        return File.builder()
                .originalFileName(originalFileName)
                .storageKey(storageKey)
                .fileUrl(fileUrl)
                .fileSize(0)
                .committed(false)
                .build();
    }

    public void markCommitted() {
        this.committed = true;
    }

    public void markDeleted() {
        this.deletedAt = Instant.now();
    }
}
