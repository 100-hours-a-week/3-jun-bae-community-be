package com.ktb.community.dto.post;

import com.ktb.community.repository.projection.PostSummaryProjection;

import java.time.Instant;

public record PostSummaryResponse(
        Long id,
        String title,
        String content,
        Long authorId,
        String authorName,
        boolean isCustomAuthor,
        String customAuthorName,
        Instant createdAt,
        Instant updatedAt,
        Long viewCount,
        Long likeCount,
        Long replyCount
) {

    public static PostSummaryResponse from(PostSummaryProjection projection) {
        boolean isCustomAuthor = projection.customAuthorName() != null;
        String authorName = isCustomAuthor ? projection.customAuthorName() : projection.authorNickname();

        return new PostSummaryResponse(
                projection.id(),
                projection.title(),
                projection.content(),
                projection.authorId(),
                authorName,
                isCustomAuthor,
                projection.customAuthorName(),
                projection.createdAt(),
                projection.updatedAt(),
                projection.viewCount(),
                projection.likeCount(),
                projection.replyCount()
        );
    }
}
