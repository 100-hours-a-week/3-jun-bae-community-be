package com.ktb.community.controller;

import com.ktb.community.dto.user.*;
import com.ktb.community.entity.User;
import com.ktb.community.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/check-email")
    public ResponseEntity<EmailCheckResponse> checkEmail(@RequestParam String email) {
        boolean available = userService.isEmailAvailable(email);
        return ResponseEntity.ok(new EmailCheckResponse(available));
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<NicknameCheckResponse> checkNickname(@RequestParam String nickname) {
        boolean available = userService.isNicknameAvailable(nickname);
        return ResponseEntity.ok(new NicknameCheckResponse(available));
    }

    @PostMapping
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegistrationRequest request) {
        User user = userService.register(request.email(), request.password(), request.nickname(), request.profileImageId());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@RequestAttribute Long userId) {

        User user = userService.getByIdOrThrow(userId);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> me(@Valid @RequestBody UserEditRequest request, @RequestAttribute Long userId) {
        User user = userService.getByIdOrThrow(userId);
        User updatedUser = userService.updateUserProfile(user.getId(), request.email(), request.nickname(), request.profileImageId());
        return ResponseEntity.status(HttpStatus.OK).body(UserResponse.from(updatedUser));
    }

    @PostMapping("/me/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody UserPasswordChangeRequest request, @RequestAttribute Long userId) {
        User user = userService.getByIdOrThrow(userId);
        userService.updateUserPassword(userId, request.oldPassword(), request.newPassword());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestAttribute Long userId) {
        User user = userService.getByIdOrThrow(userId);
        userService.markDeleted(user);
        return ResponseEntity.noContent().build();
    }
}
