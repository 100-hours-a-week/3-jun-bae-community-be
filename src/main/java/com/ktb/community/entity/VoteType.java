package com.ktb.community.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VoteType {
    AI("AI"),
    HUMAN("HUMAN");

    private final String value;
}
