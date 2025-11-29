package com.ktb.community.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "file_id")
    private File profileImage;

    @Column(name = "is_active")
    private boolean active;

    @Column(name = "is_admin")
    private boolean admin;

    @Column(name = "is_deleted")
    private boolean deleted;

    private Instant lastLoginAt;

    @CreatedDate
    @Column(nullable = false,  updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    public static User create(String email, String encodedPassword, String nickname, File profileImage, boolean admin) {
        return User.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .profileImage(profileImage)
                .admin(admin)
                .active(true)
                .deleted(false)
                .build();
    }

    public void updateProfile(String email, String nickname, File profileImage) {
        this.nickname = nickname;
        this.email = email;
        this.profileImage = profileImage;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void markDeleted() {
        this.deleted = true;
        this.active = false;
    }

    public void updateLastLogin(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
}
