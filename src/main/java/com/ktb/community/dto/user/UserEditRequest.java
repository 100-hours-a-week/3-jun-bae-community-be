package com.ktb.community.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserEditRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 2, max = 20) String nickname,
        Long profileImageId
) {
}
