package com.ktb.community.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_stats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostStats {

    @Id
    @Column(name = "post_id")
    private Long postId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "post_id", foreignKey = @ForeignKey(name = "fk_post_stats_post"))
    private Post post;

    @Column(nullable = false)
    private long likeCount;

    @Column(nullable = false)
    private long viewCount;

    @Column(nullable = false)
    private long replyCount;

    @Column(nullable = false)
    private long aiVoteCount;

    @Column(nullable = false)
    private long humanVoteCount;

    @Column(nullable = false)
    private long totalVoteCount;

    private PostStats(Post post) {
        this.post = post;
        // this.postId = post.getId();MapsId 로 자동생성, 여기서 수동으로 값 부여시 StaleObjectStateException 발생
        this.likeCount = 0L;
        this.viewCount = 0L;
        this.replyCount = 0L;
        this.aiVoteCount = 0L;
        this.humanVoteCount = 0L;
        this.totalVoteCount = 0L;
    }

    public static PostStats initialize(Post post) {
        return new PostStats(post);
    }

    void linkPost(Post post) {
        this.post = post;
        // this.postId = post.getId(); MapsId 로 자동생성, 여기서 수동으로 값 부여시 StaleObjectStateException 발생
    }

    public void incrementLike() {
        this.likeCount++;
    }

    public void decrementLike() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void incrementView() {
        this.viewCount++;
    }

    public void incrementReply() {
        this.replyCount++;
    }

    public void decrementReply() {
        if (this.replyCount > 0) {
            this.replyCount--;
        }
    }

    public void incrementAiVote() {
        this.aiVoteCount++;
        this.totalVoteCount++;
    }

    public void incrementHumanVote() {
        this.humanVoteCount++;
        this.totalVoteCount++;
    }
}
