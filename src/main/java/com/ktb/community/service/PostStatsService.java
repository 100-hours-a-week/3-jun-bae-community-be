package com.ktb.community.service;

import com.ktb.community.entity.Post;
import com.ktb.community.entity.PostStats;
import com.ktb.community.repository.PostRepository;
import com.ktb.community.repository.PostStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostStatsService {

    private final PostStatsRepository postStatsRepository;
    private final PostRepository postRepository;

    @Transactional
    public PostStats initialize(Post post) {
        if (post.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post must be saved before initializing stats");
        }
        return postStatsRepository.findByPostId(post.getId())
                .orElseGet(() -> {
                    PostStats stats = PostStats.initialize(post);
                    post.attachStats(stats);
                    return postStatsRepository.save(stats);
                });
    }

    @Transactional
    public PostStats increaseView(Long postId) {
        PostStats stats = getOrCreate(postId);
        stats.incrementView();
        return stats;
    }

    @Transactional
    public PostStats increaseLike(Long postId) {
        PostStats stats = getOrCreate(postId);
        stats.incrementLike();
        return stats;
    }

    @Transactional
    public PostStats decreaseLike(Long postId) {
        PostStats stats = getOrCreate(postId);
        stats.decrementLike();
        return stats;
    }

    @Transactional
    public PostStats increaseReply(Long postId) {
        PostStats stats = getOrCreate(postId);
        stats.incrementReply();
        return stats;
    }

    @Transactional
    public PostStats decreaseReply(Long postId) {
        PostStats stats = getOrCreate(postId);
        stats.decrementReply();
        return stats;
    }

    public PostStats getStats(Long postId) {
        return getOrCreate(postId);
    }

    public PostStats getOrCreate(Long postId) {
        return postStatsRepository.findByPostId(postId)
                .orElseGet(() -> {
                    Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
                    PostStats stats = PostStats.initialize(post);
                    post.attachStats(stats);
                    return postStatsRepository.save(stats);
                });
    }
}
