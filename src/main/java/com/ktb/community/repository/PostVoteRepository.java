package com.ktb.community.repository;

import com.ktb.community.entity.PostVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostVoteRepository extends JpaRepository<PostVote, Long> {

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    Optional<PostVote> findByPostIdAndUserId(Long postId, Long userId);

    @Query("SELECT COUNT(v) FROM PostVote v WHERE v.user.id = :userId AND v.isCorrect = true")
    long countCorrectVotesByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(v) FROM PostVote v WHERE v.user.id = :userId")
    long countTotalVotesByUserId(@Param("userId") Long userId);
}
