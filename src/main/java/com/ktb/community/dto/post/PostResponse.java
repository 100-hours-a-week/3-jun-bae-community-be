package com.ktb.community.dto.post;

import com.ktb.community.entity.File;
import com.ktb.community.entity.Post;
import com.ktb.community.entity.PostStats;
import com.ktb.community.dto.user.UserResponse;

import java.time.Instant;
import java.util.List;

public record PostResponse(
        Long id,
        String title,
        String content,
        UserResponse author,
        List<String> fileUrls,
        Instant createdAt,
        Instant updatedAt,
        long viewCount,
        long likeCount,
        long replyCount
) {

    public static PostResponse from(Post post) {
        List<String> fileUrls = post.getFiles().stream()
                .map(File::getFileUrl)
                .toList();
        PostStats stats = post.getStats();
        long viewCount = stats != null ? stats.getViewCount() : 0L;
        long likeCount = stats != null ? stats.getLikeCount() : 0L;
        long replyCount = stats != null ? stats.getReplyCount() : 0L;
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                UserResponse.from(post.getUser()),
                fileUrls,
                post.getCreatedAt(),
                post.getUpdatedAt(),
                viewCount,
                likeCount,
                replyCount
        );
    }
}
