package com.ktb.community.service;

import com.ktb.community.entity.*;
import com.ktb.community.dto.post.PostSummaryResponse;
import com.ktb.community.repository.FileRepository;
import com.ktb.community.repository.PostLikeRepository;
import com.ktb.community.repository.PostRepository;
import com.ktb.community.repository.projection.PostSummaryProjection;
import com.ktb.community.support.CursorPage;
import com.ktb.community.support.PostSortType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

import static com.ktb.community.support.Util.checkStringLengthOrThrow;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final FileRepository fileRepository;
    private final OwnershipVerifier ownershipVerifier;
    private final PostStatsService postStatsService;
    private final PostLikeRepository postLikeRepository;

    @Transactional
    public Post createPost(User author, String title, String content, List<Long> fileIds) {
        checkStringLengthOrThrow(title, 150);
        checkStringLengthOrThrow(content, 20000);
        Post post = Post.create(author, title, content);
        attachFiles(post, fileIds);
        Post saved = postRepository.save(post);
        postStatsService.initialize(saved);
        return saved;
    }

    @Transactional
    public Post createPost(User author, String title, String content, List<Long> fileIds,
                          AuthorType authorType, String customAuthorName, Integer voteDeadlineHours) {
        checkStringLengthOrThrow(title, 150);
        checkStringLengthOrThrow(content, 20000);

        Instant voteDeadlineAt = null;
        if (voteDeadlineHours != null && voteDeadlineHours > 0) {
            voteDeadlineAt = Instant.now().plusSeconds(voteDeadlineHours * 3600L);
        }

        Post post = Post.create(author, title, content, authorType, customAuthorName, voteDeadlineAt);
        attachFiles(post, fileIds);
        Post saved = postRepository.save(post);
        postStatsService.initialize(saved);
        return saved;
    }

    public CursorPage<PostSummaryResponse> getPosts(Long cursorId, int size, PostSortType sortType) {
        CursorPage<PostSummaryProjection> page = postRepository.findAllByCursor(cursorId, size, sortType);
        List<PostSummaryResponse> responses = page.getContents().stream()
                .map(PostSummaryResponse::from)
                .toList();
        return new CursorPage<>(responses, page.getNextCursor(), page.isHasNext());
    }

    public Post getPostOrThrow(Long postId) {
        return postRepository.findWithFilesByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
    }

    @Transactional
    public Post viewPost(Long postId) {
        Post post = getPostOrThrow(postId);
        postStatsService.increaseView(postId);
        return post;
    }

    @Transactional
    public Post updatePost(Long postId, User user, String title, String content, List<Long> fileIds) {
        checkStringLengthOrThrow(title, 150);
        checkStringLengthOrThrow(content, 20000);
        Post post = getPostOrThrow(postId);
        ownershipVerifier.check(post, user, "Only author can modify this post");
        post.update(title, content);
        if (fileIds != null) {
            List<File> files = loadFiles(fileIds);
            post.replaceAttachments(files);
            files.forEach(File::markCommitted);
        }
        return post;
    }

    @Transactional
    public void deletePost(Long postId, User user) {
        Post post = getPostOrThrow(postId);
        ownershipVerifier.check(post, user, "Only author can modify this post");
        post.softDelete();
    }

    private void attachFiles(Post post, List<Long> fileIds) {
        if (CollectionUtils.isEmpty(fileIds)) {
            return;
        }
        List<File> files = loadFiles(fileIds);
        post.addAttachments(files);
        files.forEach(File::markCommitted);
    }

    private List<File> loadFiles(List<Long> fileIds) {
        List<File> files = fileRepository.findByIdIn(fileIds);
        if (files.size() != fileIds.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more files not found");
        }
        return files;
    }

    public PostLikeResult checkPostLiked(Long postId, User user) {
        boolean alreadyLiked = postLikeRepository.existsByPostIdAndUserId(postId, user.getId());
        long likeCount;
        likeCount = postStatsService.getStats(postId).getLikeCount();
        return new PostLikeResult(postId, alreadyLiked, likeCount);
    }

    @Transactional
    public PostLikeResult likePost(Long postId, User user) {
        Post post = getPostOrThrow(postId);
        boolean alreadyLiked = postLikeRepository.existsByPostIdAndUserId(postId, user.getId());
        long likeCount;

        if (alreadyLiked) {
            likeCount = postStatsService.getStats(postId).getLikeCount();
        } else {
            postLikeRepository.save(PostLike.of(user, post));
            likeCount = postStatsService.increaseLike(postId).getLikeCount();
        }

        return new PostLikeResult(postId, true, likeCount);
    }

    @Transactional
    public PostLikeResult unlikePost(Long postId, User user) {
        Post post = getPostOrThrow(postId);
        return postLikeRepository.findByPostIdAndUserId(postId, user.getId())
                .map(existing -> {
                    postLikeRepository.delete(existing);
                    long likeCount = postStatsService.decreaseLike(postId).getLikeCount();
                    return new PostLikeResult(postId, false, likeCount);
                })
                .orElseGet(() -> new PostLikeResult(post.getId(), false, postStatsService.getStats(postId).getLikeCount()));
    }

    public record PostLikeResult(Long postId, boolean liked, long likeCount) {
    }
}
