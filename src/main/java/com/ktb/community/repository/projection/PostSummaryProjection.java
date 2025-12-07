package com.ktb.community.repository.projection;

import java.time.Instant;

public record PostSummaryProjection(
        Long id,
        String title,
        String content,
        Long authorId,
        String authorNickname,
        String customAuthorName,
        Instant createdAt,
        Instant updatedAt,
        Long viewCount,
        Long likeCount,
        Long replyCount
) {
}
