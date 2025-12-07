package com.ktb.community.controller;

import com.ktb.community.dto.vote.*;
import com.ktb.community.entity.User;
import com.ktb.community.service.PostVoteService;
import com.ktb.community.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class VoteController {

    private final PostVoteService postVoteService;
    private final UserService userService;

    @PostMapping("/posts/{postId}/vote")
    public ResponseEntity<Map<String, Object>> votePost(
            @PathVariable Long postId,
            @Valid @RequestBody VoteRequest request,
            @AuthenticationPrincipal UserDetails userPrincipal
    ) {
        User user = userService.getByEmailOrThrow(userPrincipal.getUsername());
        VoteResponse response = postVoteService.votePost(postId, user.getId(), request);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }

    @GetMapping("/users/me/vote-score")
    public ResponseEntity<Map<String, Object>> getUserVoteScore(
            @AuthenticationPrincipal UserDetails userPrincipal
    ) {
        User user = userService.getByEmailOrThrow(userPrincipal.getUsername());
        UserVoteScoreResponse response = postVoteService.getUserVoteScore(user.getId());
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }

    @GetMapping("/posts/{postId}/vote-stats")
    public ResponseEntity<Map<String, Object>> getPostVoteStats(@PathVariable Long postId) {
        PostVoteStatsResponse response = postVoteService.getPostVoteStats(postId);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/posts/{postId}/reveal-answer")
    public ResponseEntity<Map<String, Object>> revealAnswer(@PathVariable Long postId) {
        postVoteService.revealAnswer(postId);
        PostVoteStatsResponse response = postVoteService.getPostVoteStats(postId);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }
}
