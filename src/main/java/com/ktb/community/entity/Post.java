package com.ktb.community.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Post implements OwnedByUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private Instant deletedAt;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostFile> attachments = new ArrayList<>();

    @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PostStats stats;

    @Builder // 빌더 패턴 사용으로 가독성 향상
    private Post(User user, String title, String content) {
        this.user = user;
        this.title = title;
        this.content = content;
    }

    public static Post create(User user, String title, String content) {
        return Post.builder()
                .user(user)
                .title(title)
                .content(content)
                .build();
    }

    public void addAttachments(List<File> files) {
        files.forEach(file -> attachments.add(new PostFile(this, file)));
    }

    public void replaceAttachments(List<File> files) {
        attachments.clear();
        addAttachments(files);
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public List<File> getFiles() {
        return attachments.stream()
                .map(PostFile::getFile)
                .collect(Collectors.toList());
    }
    // PostStats 와 1대1 연결을 위해 초기 생성이 필요
    public void attachStats(PostStats stats) {
        this.stats = stats;
        if (stats != null) {
            stats.linkPost(this);
        }
    }
}
