package com.ktb.community.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthorType {
    AI("AI"),
    HUMAN("HUMAN");

    private final String value;
}
