package com.ktb.community.dto.vote;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserVoteScoreResponse {

    private Long userId;
    private String nickname;
    private int voteScore;
    private long totalVotes;
    private long correctVotes;
    private double accuracy;

    public static UserVoteScoreResponse of(Long userId, String nickname, int voteScore, long totalVotes, long correctVotes) {
        double accuracy = totalVotes > 0 ? (double) correctVotes / totalVotes * 100 : 0.0;
        return UserVoteScoreResponse.builder()
                .userId(userId)
                .nickname(nickname)
                .voteScore(voteScore)
                .totalVotes(totalVotes)
                .correctVotes(correctVotes)
                .accuracy(accuracy)
                .build();
    }
}
