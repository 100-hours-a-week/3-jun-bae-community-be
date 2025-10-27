package com.ktb.community.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserPasswordChangeRequest (
        @NotBlank @Size(min = 8, max = 50) String oldPassword,
        @NotBlank @Size(min = 8, max = 50) String newPassword
){
}
