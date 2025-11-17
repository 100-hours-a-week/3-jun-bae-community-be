package com.ktb.community.service;

import com.ktb.community.entity.File;
import com.ktb.community.entity.User;
import com.ktb.community.repository.FileRepository;
import com.ktb.community.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private FileRepository fileRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void register_withoutProfileImage_createsActiveUser() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByNickname("tester")).thenReturn(false);
        when(passwordEncoder.encode("raw")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 1L);
            return user;
        });

        User registered = userService.register("test@example.com", "raw", "tester", null);

        assertThat(registered.getId()).isEqualTo(1L);
        assertThat(registered.getPassword()).isEqualTo("encoded");
        assertThat(registered.isActive()).isTrue();
        assertThat(registered.isDeleted()).isFalse();
        verify(fileRepository, never()).findByIdAndDeletedAtIsNull(anyLong());
    }

    @Test
    void register_withProfileImage_marksFileCommitted() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByNickname("tester")).thenReturn(false);
        when(passwordEncoder.encode("raw")).thenReturn("encoded");
        File profile = File.pending("profile.png", "key", "url", 12);
        when(fileRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(profile));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registered = userService.register("test@example.com", "raw", "tester", 2L);

        assertThat(profile.isCommitted()).isTrue();
        assertThat(registered.getProfileImage()).isSameAs(profile);
    }

    @Test
    void register_whenEmailInUse_throwsConflict() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register("test@example.com", "raw", "tester", null))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void register_whenNicknameInUse_throwsConflict() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByNickname("tester")).thenReturn(true);

        assertThatThrownBy(() -> userService.register("test@example.com", "raw", "tester", null))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void register_whenProfileImageMissing_throwsNotFound() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByNickname("tester")).thenReturn(false);
        when(fileRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.register("test@example.com", "raw", "tester", 2L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void isEmailAvailable_delegatesToRepository() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);

        assertThat(userService.isEmailAvailable("test@example.com")).isTrue();
    }

    @Test
    void getByEmailOrThrow_returnsActiveUser() {
        User user = buildUser(1L, true, false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        User found = userService.getByEmailOrThrow("test@example.com");

        assertThat(found).isSameAs(user);
    }

    @Test
    void getByEmailOrThrow_whenInactive_throwsForbidden() {
        User user = buildUser(1L, false, false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.getByEmailOrThrow("test@example.com"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getByEmailOrThrow_whenDeleted_throwsForbidden() {
        User user = buildUser(1L, true, true);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.getByEmailOrThrow("test@example.com"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getByEmailOrThrow_whenNotFound_throwsNotFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByEmailOrThrow("test@example.com"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getByIdOrThrow_returnsActiveUser() {
        User user = buildUser(1L, true, false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User found = userService.getByIdOrThrow(1L);

        assertThat(found).isSameAs(user);
    }

    @Test
    void getByIdOrThrow_whenInactive_throwsForbidden() {
        User user = buildUser(1L, false, false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.getByIdOrThrow(1L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getByIdOrThrow_whenNotFound_throwsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByIdOrThrow(1L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void markDeleted_flagsUserInactive() {
        User user = buildUser(1L, true, false);

        userService.markDeleted(user);

        assertThat(user.isDeleted()).isTrue();
        assertThat(user.isActive()).isFalse();
    }

    @Test
    void updateLastLogin_updatesTimestamp() {
        User user = buildUser(1L, true, false);
        ReflectionTestUtils.setField(user, "lastLoginAt", null);

        userService.updateLastLogin(user);

        LocalDateTime lastLoginAt = (LocalDateTime) ReflectionTestUtils.getField(user, "lastLoginAt");
        assertThat(lastLoginAt).isNotNull();
    }

    private static User buildUser(Long id, boolean active, boolean deleted) {
        User user = User.builder()
                .email("user" + id + "@example.com")
                .password("pw")
                .nickname("user" + id)
                .profileImage(null)
                .active(active)
                .admin(false)
                .deleted(deleted)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
