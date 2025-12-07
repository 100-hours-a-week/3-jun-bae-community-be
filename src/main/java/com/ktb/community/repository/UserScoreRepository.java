package com.ktb.community.repository;

import com.ktb.community.entity.UserScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserScoreRepository extends JpaRepository<UserScore, Long> {

    Optional<UserScore> findByUserId(Long userId);
}
