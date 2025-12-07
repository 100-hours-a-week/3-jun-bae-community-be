package com.ktb.community.repository;

import com.ktb.community.entity.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, PostQueryRepository {

    Optional<Post> findByIdAndDeletedAtIsNull(Long id);

    @EntityGraph(attributePaths = {"user", "attachments", "attachments.file", "stats"})
    Optional<Post> findWithFilesByIdAndDeletedAtIsNull(Long id);

    @Query("SELECT p FROM Post p WHERE p.voteDeadlineAt < :now AND p.answerRevealedAt IS NULL AND p.deletedAt IS NULL")
    List<Post> findByVoteDeadlineAtBeforeAndAnswerRevealedAtIsNull(@Param("now") Instant now);
}
