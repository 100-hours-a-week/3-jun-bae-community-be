package com.ktb.community.support;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

public enum PostSortType {
    LATEST(List.of("latest", "recent")),
    LIKES(List.of("likes", "like")),
    COMMENTS(List.of("comments", "comment", "replies", "reply"));

    private final List<String> aliases;

    PostSortType(List<String> aliases) {
        this.aliases = aliases;
    }

    public static PostSortType from(String value) {
        if (value == null || value.isBlank()) {
            return LATEST;
        }
        return Arrays.stream(values())
                .filter(sort -> sort.aliases.stream()
                        .anyMatch(alias -> alias.equalsIgnoreCase(value)))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Unsupported sort type: " + value));
    }
}
