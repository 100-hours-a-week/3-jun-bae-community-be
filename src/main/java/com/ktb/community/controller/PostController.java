package com.ktb.community.controller;

import com.ktb.community.dto.post.PostCreateRequest;
import com.ktb.community.dto.post.PostCursorResponse;
import com.ktb.community.dto.post.PostLikeResponse;
import com.ktb.community.dto.post.PostResponse;
import com.ktb.community.dto.post.PostSummaryResponse;
import com.ktb.community.dto.post.PostUpdateRequest;
import com.ktb.community.entity.Post;
import com.ktb.community.entity.User;
import com.ktb.community.service.PostService;
import com.ktb.community.service.UserService;
import com.ktb.community.support.CursorPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<PostCursorResponse> list(@RequestParam(required = false) Long cursorId,
                                                   @RequestParam(defaultValue = "10") int size) {
        int pageSize = Math.min(Math.max(size, 1), 50);
        CursorPage<PostSummaryResponse> page = postService.getPosts(cursorId, pageSize);
        return ResponseEntity.ok(PostCursorResponse.from(page));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> get(@PathVariable Long postId) {
        Post post = postService.viewPost(postId);
        return ResponseEntity.ok(PostResponse.from(post));
    }

    @PostMapping
    public ResponseEntity<PostResponse> create(@Valid @RequestBody PostCreateRequest request,
                                               @RequestAttribute Long userId) {
        User user = userService.getByIdOrThrow(userId);
        Post post = postService.createPost(user, request.title(), request.content(), request.fileIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(PostResponse.from(post));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> update(@PathVariable Long postId,
                                               @Valid @RequestBody PostUpdateRequest request,
                                               @RequestAttribute Long userId) {
        User user = userService.getByIdOrThrow(userId);
        Post updated = postService.updatePost(postId, user, request.title(), request.content(), request.fileIds());
        return ResponseEntity.ok(PostResponse.from(updated));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> delete(@PathVariable Long postId,
                                       @RequestAttribute Long userId) {
        User user = userService.getByIdOrThrow(userId);
        postService.deletePost(postId, user);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{postId}/likes")
    public ResponseEntity<PostLikeResponse> getLikes(@PathVariable Long postId, @RequestAttribute Long userId) {
        User user = userService.getByIdOrThrow(userId);
        return ResponseEntity.ok(PostLikeResponse.from(postService.checkPostLiked(postId, user)));
    }

    @PostMapping("/{postId}/likes")
    public ResponseEntity<PostLikeResponse> like(@PathVariable Long postId,
                                                 @RequestAttribute Long userId) {
        User user = userService.getByIdOrThrow(userId);
        return ResponseEntity.ok(PostLikeResponse.from(postService.likePost(postId, user)));
    }

    @DeleteMapping("/{postId}/likes")
    public ResponseEntity<PostLikeResponse> unlike(@PathVariable Long postId,
                                                   @RequestAttribute Long userId) {
        User user = userService.getByIdOrThrow(userId); // 유저 확인
        return ResponseEntity.ok(PostLikeResponse.from(postService.unlikePost(postId, user)));
    }

}
