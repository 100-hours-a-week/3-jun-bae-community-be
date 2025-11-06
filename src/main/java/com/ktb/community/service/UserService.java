package com.ktb.community.service;

import com.ktb.community.entity.File;
import com.ktb.community.entity.User;
import com.ktb.community.repository.FileRepository;
import com.ktb.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(String email, String rawPassword, String nickname, Long profileImageId) {
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nickname already in use");
        }

        File profileImage = null;
        if (profileImageId != null) {
            profileImage = fileRepository.findByIdAndDeletedAtIsNull(profileImageId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile image not found"));
            profileImage.markCommitted();
        }

        User user = User.create(email, passwordEncoder.encode(rawPassword), nickname, profileImage, false);
        return userRepository.save(user);
    }
    @Transactional
    public User updateUserProfile(Long userId, String email, String nickname, Long profileImageId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        File profileImage = null;
        if(profileImageId != null) {
            profileImage = fileRepository.findByIdAndDeletedAtIsNull(profileImageId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile image not found"));
        }
        user.updateProfile(email, nickname, profileImage);
        return user;
    }

    @Transactional
    public void updateUserPassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (passwordEncoder.matches(oldPassword, user.getPassword())) {
            String encodedNewPassword = passwordEncoder.encode(newPassword);
            user.updatePassword(encodedNewPassword);
        }
        else{
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Old password is not correct");
        }
    }

    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

    public User getByEmailOrThrow(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        ensureActive(user);
        return user;
    }

    public User getByIdOrThrow(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        ensureActive(user);
        return user;
    }

    @Transactional
    public void markDeleted(User user) {
        user.markDeleted();
    }

    @Transactional
    public void updateLastLogin(User user) {
        user.updateLastLogin(LocalDateTime.now());
    }

    private void ensureActive(User user) {
        if (user.isDeleted() || !user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is inactive");
        }
    }
}
