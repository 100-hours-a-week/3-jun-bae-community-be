package com.ktb.community.dto.vote;

import com.ktb.community.entity.VoteType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class VoteResponse {

    private Long voteId;
    private VoteType voteType;
    private boolean isCorrect;
    private int userTotalScore;
    private VoteStatsDto postVoteStats;
    private Instant createdAt;

    @Getter
    @Builder
    public static class VoteStatsDto {
        private long aiVoteCount;
        private long humanVoteCount;
        private long totalVoteCount;
    }
}
