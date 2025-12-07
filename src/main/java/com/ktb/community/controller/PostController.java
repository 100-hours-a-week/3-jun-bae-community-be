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
import com.ktb.community.support.PostSortType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<PostCursorResponse> list(@RequestParam(required = false) Long cursorId,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @RequestParam(name = "sort", defaultValue = "latest") String sort) {
        int pageSize = Math.min(Math.max(size, 1), 50);
        CursorPage<PostSummaryResponse> page = postService.getPosts(cursorId, pageSize,
                PostSortType.from(sort));
        return ResponseEntity.ok(PostCursorResponse.from(page));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> get(@PathVariable Long postId) {
        Post post = postService.viewPost(postId);
        return ResponseEntity.ok(PostResponse.from(post));
    }

    @PostMapping
    public ResponseEntity<PostResponse> create(@Valid @RequestBody PostCreateRequest request,
                                               @AuthenticationPrincipal UserDetails principal) {
        ensureAuthenticated(principal);
        User user = userService.getByEmailOrThrow(principal.getUsername());

        Post post;
        if (request.authorType() != null && user.isAdmin()) {
            post = postService.createPost(user, request.title(), request.content(), request.fileIds(),
                    request.authorType(), request.customAuthorName(), request.voteDeadlineHours());
        } else {
            post = postService.createPost(user, request.title(), request.content(), request.fileIds());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(PostResponse.from(post));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> update(@PathVariable Long postId,
                                               @Valid @RequestBody PostUpdateRequest request,
                                               @AuthenticationPrincipal UserDetails principal) {
        ensureAuthenticated(principal);
        User user = userService.getByEmailOrThrow(principal.getUsername());
        Post updated = postService.updatePost(postId, user, request.title(), request.content(), request.fileIds());
        return ResponseEntity.ok(PostResponse.from(updated));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> delete(@PathVariable Long postId,
                                       @AuthenticationPrincipal UserDetails principal) {
        ensureAuthenticated(principal);
        User user = userService.getByEmailOrThrow(principal.getUsername());
        postService.deletePost(postId, user);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{postId}/likes")
    public ResponseEntity<PostLikeResponse> getLikes(@PathVariable Long postId, @AuthenticationPrincipal UserDetails principal) {
        ensureAuthenticated(principal);
        User user = userService.getByEmailOrThrow(principal.getUsername());
        return ResponseEntity.ok(PostLikeResponse.from(postService.checkPostLiked(postId, user)));
    }

    @PostMapping("/{postId}/likes")
    public ResponseEntity<PostLikeResponse> like(@PathVariable Long postId,
                                                 @AuthenticationPrincipal UserDetails principal) {
        ensureAuthenticated(principal);
        User user = userService.getByEmailOrThrow(principal.getUsername());
        return ResponseEntity.ok(PostLikeResponse.from(postService.likePost(postId, user)));
    }

    @DeleteMapping("/{postId}/likes")
    public ResponseEntity<PostLikeResponse> unlike(@PathVariable Long postId,
                                                   @AuthenticationPrincipal UserDetails principal) {
        ensureAuthenticated(principal);
        User user = userService.getByEmailOrThrow(principal.getUsername());
        return ResponseEntity.ok(PostLikeResponse.from(postService.unlikePost(postId, user)));
    }

    private void ensureAuthenticated(UserDetails principal) {
        if (principal == null) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
    }
}
