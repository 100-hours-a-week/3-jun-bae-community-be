package com.ktb.community.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "post_vote",
    uniqueConstraints = @UniqueConstraint(
        name = "unique_user_post_vote",
        columnNames = {"user_id", "post_id"}
    ),
    indexes = {
        @Index(name = "idx_post_vote_post_id", columnList = "post_id"),
        @Index(name = "idx_post_vote_user_id", columnList = "user_id")
    }
)
public class PostVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private VoteType voteType;

    @Column(nullable = false)
    private boolean isCorrect;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    private PostVote(User user, Post post, VoteType voteType, boolean isCorrect) {
        this.user = user;
        this.post = post;
        this.voteType = voteType;
        this.isCorrect = isCorrect;
    }

    public static PostVote create(User user, Post post, VoteType voteType) {
        boolean isCorrect = calculateCorrectness(post, voteType);
        return PostVote.builder()
                .user(user)
                .post(post)
                .voteType(voteType)
                .isCorrect(isCorrect)
                .build();
    }

    private static boolean calculateCorrectness(Post post, VoteType voteType) {
        if (post.getAuthorType() == null) {
            return false;
        }
        return post.getAuthorType().name().equals(voteType.name());
    }
}
