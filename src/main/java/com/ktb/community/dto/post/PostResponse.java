package com.ktb.community.dto.post;

import com.ktb.community.entity.AuthorType;
import com.ktb.community.entity.File;
import com.ktb.community.entity.Post;
import com.ktb.community.entity.PostStats;
import com.ktb.community.entity.VoteType;
import com.ktb.community.dto.user.UserResponse;

import java.time.Instant;
import java.util.List;

public record PostResponse(
        Long id,
        String title,
        String content,
        UserResponse author,
        String authorName,
        boolean isCustomAuthor,
        String customAuthorName,
        AuthorType authorType,
        boolean answerRevealed,
        Instant voteDeadlineAt,
        Instant answerRevealedAt,
        CurrentUserVote currentUserVote,
        VoteStats voteStats,
        List<String> fileUrls,
        Instant createdAt,
        Instant updatedAt,
        long viewCount,
        long likeCount,
        long replyCount
) {

    public record CurrentUserVote(
            VoteType voteType,
            boolean isCorrect
    ) {}

    public record VoteStats(
            long aiVoteCount,
            long humanVoteCount,
            long totalVoteCount
    ) {}

    public static PostResponse from(Post post) {
        return from(post, null);
    }

    public static PostResponse from(Post post, CurrentUserVote currentUserVote) {
        List<String> fileUrls = post.getFiles().stream()
                .map(File::getFileUrl)
                .toList();
        PostStats stats = post.getStats();
        long viewCount = stats != null ? stats.getViewCount() : 0L;
        long likeCount = stats != null ? stats.getLikeCount() : 0L;
        long replyCount = stats != null ? stats.getReplyCount() : 0L;

        boolean isCustomAuthor = post.getCustomAuthorName() != null;
        String authorName = post.getDisplayAuthorName();
        boolean answerRevealed = post.isAnswerRevealed();
        AuthorType authorType = answerRevealed ? post.getAuthorType() : null;

        VoteStats voteStats = null;
        if (stats != null) {
            voteStats = new VoteStats(
                    stats.getAiVoteCount(),
                    stats.getHumanVoteCount(),
                    stats.getTotalVoteCount()
            );
        }

        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                UserResponse.from(post.getUser()),
                authorName,
                isCustomAuthor,
                post.getCustomAuthorName(),
                authorType,
                answerRevealed,
                post.getVoteDeadlineAt(),
                post.getAnswerRevealedAt(),
                currentUserVote,
                voteStats,
                fileUrls,
                post.getCreatedAt(),
                post.getUpdatedAt(),
                viewCount,
                likeCount,
                replyCount
        );
    }
}
