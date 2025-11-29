package com.ktb.community.dto.comment;

import com.ktb.community.entity.Comment;

import java.time.Instant;

public record CommentResponse(
        Long id,
        String content,
        Long authorId,
        String authorNickname,
        Instant createdAt,
        Instant updatedAt
) {

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getId(),
                comment.getUser().getNickname(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
