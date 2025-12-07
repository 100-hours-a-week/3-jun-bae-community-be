package com.ktb.community.dto.vote;

import com.ktb.community.entity.VoteType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VoteRequest {

    @NotNull(message = "투표 유형은 필수입니다.")
    private VoteType voteType;

    public VoteRequest(VoteType voteType) {
        this.voteType = voteType;
    }
}
