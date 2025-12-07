package com.ktb.community.repository;

import com.ktb.community.entity.PostVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostVoteRepository extends JpaRepository<PostVote, Long> {

    @Query("SELECT COUNT(v) > 0 FROM PostVote v WHERE v.post.id = :postId AND v.user.id = :userId")
    boolean existsByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);

    @Query("SELECT v FROM PostVote v JOIN FETCH v.post JOIN FETCH v.user WHERE v.post.id = :postId AND v.user.id = :userId")
    Optional<PostVote> findByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);

    @Query("SELECT COUNT(v) FROM PostVote v WHERE v.user.id = :userId AND v.isCorrect = true")
    long countCorrectVotesByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(v) FROM PostVote v WHERE v.user.id = :userId")
    long countTotalVotesByUserId(@Param("userId") Long userId);
}
