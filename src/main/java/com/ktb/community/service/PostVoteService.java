package com.ktb.community.service;

import com.ktb.community.dto.vote.*;
import com.ktb.community.entity.*;
import com.ktb.community.repository.PostRepository;
import com.ktb.community.repository.PostVoteRepository;
import com.ktb.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostVoteService {

    private final PostRepository postRepository;
    private final PostVoteRepository postVoteRepository;
    private final UserRepository userRepository;
    private final PostStatsService postStatsService;
    private final UserScoreService userScoreService;

    @Transactional
    public VoteResponse votePost(Long postId, Long userId, VoteRequest request) {
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!post.canVote()) {
            throw new IllegalStateException("투표가 마감된 게시글입니다.");
        }

        if (postVoteRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new IllegalStateException("이미 투표한 게시글입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        PostVote vote = PostVote.create(user, post, request.getVoteType());
        postVoteRepository.save(vote);

        PostStats stats = postStatsService.getOrCreate(post.getId());
        if (request.getVoteType() == VoteType.AI) {
            stats.incrementAiVote();
        } else {
            stats.incrementHumanVote();
        }

        if (vote.isCorrect()) {
            userScoreService.incrementCorrectVote(user);
        } else {
            userScoreService.incrementTotalVote(user);
        }

        UserScore userScore = userScoreService.getUserScore(userId);
        int totalScore = userScore != null ? userScore.getVoteScore() : 0;

        return VoteResponse.builder()
                .voteId(vote.getId())
                .voteType(vote.getVoteType())
                .isCorrect(vote.isCorrect())
                .userTotalScore(totalScore)
                .postVoteStats(VoteResponse.VoteStatsDto.builder()
                        .aiVoteCount(stats.getAiVoteCount())
                        .humanVoteCount(stats.getHumanVoteCount())
                        .totalVoteCount(stats.getTotalVoteCount())
                        .build())
                .createdAt(vote.getCreatedAt())
                .build();
    }

    public UserVoteScoreResponse getUserVoteScore(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        UserScore userScore = userScoreService.getUserScore(userId);

        if (userScore == null) {
            return UserVoteScoreResponse.of(user.getId(), user.getNickname(), 0, 0, 0);
        }

        return UserVoteScoreResponse.of(
                user.getId(),
                user.getNickname(),
                userScore.getVoteScore(),
                userScore.getTotalVotes(),
                userScore.getCorrectVotes()
        );
    }

    public PostVoteStatsResponse getPostVoteStats(Long postId) {
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        PostStats stats = postStatsService.getOrCreate(post.getId());

        return PostVoteStatsResponse.of(
                post.getId(),
                stats.getAiVoteCount(),
                stats.getHumanVoteCount(),
                post.isAnswerRevealed(),
                post.getAuthorType(),
                post.getVoteDeadlineAt()
        );
    }

    @Transactional
    public void revealAnswer(Long postId) {
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        post.revealAnswer();
    }

    public PostVote getCurrentUserVote(Long postId, Long userId) {
        return postVoteRepository.findByPostIdAndUserId(postId, userId)
                .orElse(null);
    }
}
