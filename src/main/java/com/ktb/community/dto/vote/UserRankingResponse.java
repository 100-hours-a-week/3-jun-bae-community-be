package com.ktb.community.dto.vote;

import com.ktb.community.entity.UserScore;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserRankingResponse {

    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private int voteScore;
    private long totalVotes;
    private long correctVotes;
    private double accuracy;
    private int rank;

    public static UserRankingResponse of(UserScore userScore, int rank) {
        double accuracy = userScore.getTotalVotes() > 0
                ? (double) userScore.getCorrectVotes() / userScore.getTotalVotes() * 100
                : 0.0;

        String profileImageUrl = userScore.getUser().getProfileImage() != null
                ? userScore.getUser().getProfileImage().getFileUrl()
                : null;

        return UserRankingResponse.builder()
                .userId(userScore.getUserId())
                .nickname(userScore.getUser().getNickname())
                .profileImageUrl(profileImageUrl)
                .voteScore(userScore.getVoteScore())
                .totalVotes(userScore.getTotalVotes())
                .correctVotes(userScore.getCorrectVotes())
                .accuracy(accuracy)
                .rank(rank)
                .build();
    }

    @Getter
    @Builder
    public static class RankingListResponse {
        private List<UserRankingResponse> rankings;
        private int totalUsers;
        private UserRankingResponse myRanking;
    }
}
