package com.ktb.community.service;

import com.ktb.community.dto.vote.UserRankingResponse;
import com.ktb.community.entity.User;
import com.ktb.community.entity.UserScore;
import com.ktb.community.repository.UserScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserScoreService {

    private final UserScoreRepository userScoreRepository;

    @Transactional
    public UserScore getOrCreateUserScore(User user) {
        return userScoreRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserScore newScore = UserScore.initialize(user);
                    return userScoreRepository.save(newScore);
                });
    }

    @Transactional
    public void incrementCorrectVote(User user) {
        UserScore score = getOrCreateUserScore(user);
        score.incrementScore();
    }

    @Transactional
    public void incrementTotalVote(User user) {
        UserScore score = getOrCreateUserScore(user);
        score.incrementTotalVotes();
    }

    public UserScore getUserScore(Long userId) {
        return userScoreRepository.findByUserId(userId)
                .orElse(null);
    }

    public UserRankingResponse.RankingListResponse getTopRankings(int limit, Long currentUserId) {
        Pageable pageable = PageRequest.of(0, limit);
        List<UserScore> topScores = userScoreRepository.findTopRankings(pageable);

        List<UserRankingResponse> rankings = IntStream.range(0, topScores.size())
                .mapToObj(i -> UserRankingResponse.of(topScores.get(i), i + 1))
                .toList();

        long totalUsers = userScoreRepository.countActiveUsers();

        UserRankingResponse myRanking = null;
        if (currentUserId != null) {
            UserScore myScore = userScoreRepository.findByUserId(currentUserId).orElse(null);
            if (myScore != null && myScore.getVoteScore() > 0) {
                int myRank = calculateRank(myScore);
                myRanking = UserRankingResponse.of(myScore, myRank);
            }
        }

        return UserRankingResponse.RankingListResponse.builder()
                .rankings(rankings)
                .totalUsers((int) totalUsers)
                .myRanking(myRanking)
                .build();
    }

    private int calculateRank(UserScore userScore) {
        long higherRanked = userScoreRepository.countUsersRankedHigherThan(
                userScore.getVoteScore(),
                userScore.getCorrectVotes(),
                userScore.getUserId()
        );
        return (int) higherRanked + 1;
    }
}
