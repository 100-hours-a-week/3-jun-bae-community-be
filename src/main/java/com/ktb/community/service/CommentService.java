package com.ktb.community.service;

import com.ktb.community.entity.Comment;
import com.ktb.community.entity.Post;
import com.ktb.community.entity.User;
import com.ktb.community.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static com.ktb.community.support.Util.checkStringLengthOrThrow;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService;
    private final OwnershipVerifier ownershipVerifier;
    private final PostStatsService postStatsService;

    @Transactional
    public Comment addComment(Long postId, User user, String content) {
        checkStringLengthOrThrow(content, 150);
        Post post = postService.getPostOrThrow(postId);
        Comment comment = Comment.create(user, post, content);
        if(!checkStringLengthOrThrow(comment.getContent(), 150)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Too Long Content.");
        }
        Comment saved = commentRepository.save(comment);
        postStatsService.increaseReply(postId);
        return saved;
    }

    public Page<Comment> getComments(Long postId, Pageable pageable) {
        postService.getPostOrThrow(postId);
        return commentRepository.findPageByPostId(postId, pageable);
    }

    @Transactional
    public Comment updateComment(Long commentId, User user, String content) {
        checkStringLengthOrThrow(content, 150);
        Comment comment = getActiveComment(commentId);
        ownershipVerifier.check(comment, user, "Only author can modify this comment");
        comment.updateContent(content);
        return comment;
    }

    @Transactional
    public void deleteComment(Long commentId, User user) {
        Comment comment = getActiveComment(commentId);
        ownershipVerifier.check(comment, user, "Only author can modify this comment");
        comment.softDelete();
        postStatsService.decreaseReply(comment.getPost().getId());
    }

    private Comment getActiveComment(Long commentId) {
        return commentRepository.findById(commentId)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
    }
}
