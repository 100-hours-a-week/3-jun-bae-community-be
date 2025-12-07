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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "user_score",
    indexes = {
        @Index(name = "idx_user_score_user_id", columnList = "user_id"),
        @Index(name = "idx_user_score_score", columnList = "vote_score DESC")
    }
)
public class UserScore {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_user_score_user"))
    private User user;

    @Column(nullable = false)
    private int voteScore;

    @Column(nullable = false)
    private long totalVotes;

    @Column(nullable = false)
    private long correctVotes;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @Builder
    private UserScore(User user, int voteScore, long totalVotes, long correctVotes) {
        this.user = user;
        this.voteScore = voteScore;
        this.totalVotes = totalVotes;
        this.correctVotes = correctVotes;
    }

    public static UserScore initialize(User user) {
        return UserScore.builder()
                .user(user)
                .voteScore(0)
                .totalVotes(0)
                .correctVotes(0)
                .build();
    }

    public void incrementScore() {
        this.voteScore++;
        this.correctVotes++;
        this.totalVotes++;
    }

    public void incrementTotalVotes() {
        this.totalVotes++;
    }

    public double getAccuracy() {
        return totalVotes > 0 ? (double) correctVotes / totalVotes * 100 : 0.0;
    }
}
