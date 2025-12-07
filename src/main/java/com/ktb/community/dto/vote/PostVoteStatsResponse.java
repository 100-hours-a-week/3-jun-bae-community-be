package com.ktb.community.dto.vote;

import com.ktb.community.entity.AuthorType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class PostVoteStatsResponse {

    private Long postId;
    private long aiVoteCount;
    private long humanVoteCount;
    private long totalVoteCount;
    private double aiPercentage;
    private double humanPercentage;
    private boolean answerRevealed;
    private AuthorType authorType;
    private Instant voteDeadlineAt;

    public static PostVoteStatsResponse of(Long postId, long aiVoteCount, long humanVoteCount,
                                          boolean answerRevealed, AuthorType authorType, Instant voteDeadlineAt) {
        long totalVoteCount = aiVoteCount + humanVoteCount;
        double aiPercentage = totalVoteCount > 0 ? (double) aiVoteCount / totalVoteCount * 100 : 0.0;
        double humanPercentage = totalVoteCount > 0 ? (double) humanVoteCount / totalVoteCount * 100 : 0.0;

        return PostVoteStatsResponse.builder()
                .postId(postId)
                .aiVoteCount(aiVoteCount)
                .humanVoteCount(humanVoteCount)
                .totalVoteCount(totalVoteCount)
                .aiPercentage(aiPercentage)
                .humanPercentage(humanPercentage)
                .answerRevealed(answerRevealed)
                .authorType(answerRevealed ? authorType : null)
                .voteDeadlineAt(voteDeadlineAt)
                .build();
    }
}
