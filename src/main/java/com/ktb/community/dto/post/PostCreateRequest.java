package com.ktb.community.dto.post;

import com.ktb.community.entity.AuthorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PostCreateRequest(
        @NotBlank @Size(max = 150) String title,
        @NotBlank String content,
        List<Long> fileIds,
        AuthorType authorType,
        String customAuthorName,
        Integer voteDeadlineHours
) {
}
