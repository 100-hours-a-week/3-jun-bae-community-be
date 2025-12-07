package com.ktb.community.service;

import com.ktb.community.entity.User;
import com.ktb.community.entity.UserScore;
import com.ktb.community.repository.UserScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
