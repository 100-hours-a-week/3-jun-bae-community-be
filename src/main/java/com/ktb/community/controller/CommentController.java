package com.ktb.community.controller;

import com.ktb.community.dto.comment.CommentCreateRequest;
import com.ktb.community.dto.comment.CommentPageResponse;
import com.ktb.community.dto.comment.CommentResponse;
import com.ktb.community.dto.comment.CommentUpdateRequest;
import com.ktb.community.entity.Comment;
import com.ktb.community.entity.User;
import com.ktb.community.service.CommentService;
import com.ktb.community.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentPageResponse> list(@PathVariable Long postId,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        int pageIndex = Math.max(page, 0);
        int pageSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        Page<CommentResponse> commentPage = commentService.getComments(postId, pageable)
                .map(CommentResponse::from);
        return ResponseEntity.ok(CommentPageResponse.from(commentPage));
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> create(@PathVariable Long postId,
                                                  @Valid @RequestBody CommentCreateRequest request,
                                                  @RequestAttribute Long userId) {
        User user = userService.getByIdOrThrow(userId);
        Comment comment = commentService.addComment(postId, user, request.content());
        return ResponseEntity.status(HttpStatus.CREATED).body(CommentResponse.from(comment));
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> update(@PathVariable Long commentId,
                                                  @Valid @RequestBody CommentUpdateRequest request,
                                                  @RequestAttribute Long userId) {
        User user = userService.getByIdOrThrow(userId);
        Comment updated = commentService.updateComment(commentId, user, request.content());
        return ResponseEntity.ok(CommentResponse.from(updated));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> delete(@PathVariable Long commentId,
                                       @RequestAttribute Long userId) {
        User user = userService.getByIdOrThrow(userId);
        commentService.deleteComment(commentId, user);
        return ResponseEntity.noContent().build();
    }
}
