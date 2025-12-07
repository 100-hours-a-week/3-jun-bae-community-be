package com.ktb.community.repository;

import com.ktb.community.entity.UserScore;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserScoreRepository extends JpaRepository<UserScore, Long> {

    Optional<UserScore> findByUserId(Long userId);

    @Query("SELECT us FROM UserScore us JOIN FETCH us.user WHERE us.voteScore > 0 ORDER BY us.voteScore DESC, us.correctVotes DESC, us.userId ASC")
    List<UserScore> findTopRankings(Pageable pageable);

    @Query("SELECT COUNT(us) FROM UserScore us WHERE us.voteScore > :score OR (us.voteScore = :score AND us.correctVotes > :correctVotes) OR (us.voteScore = :score AND us.correctVotes = :correctVotes AND us.userId < :userId)")
    long countUsersRankedHigherThan(@Param("score") int score, @Param("correctVotes") long correctVotes, @Param("userId") Long userId);

    @Query("SELECT COUNT(us) FROM UserScore us WHERE us.voteScore > 0")
    long countActiveUsers();
}
