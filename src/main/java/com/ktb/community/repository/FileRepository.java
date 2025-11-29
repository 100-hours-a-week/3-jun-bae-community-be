package com.ktb.community.repository;

import com.ktb.community.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {

    Optional<File> findByIdAndDeletedAtIsNull(Long id);

    List<File> findByIdIn(Collection<Long> ids);

    List<File> findByCommittedFalseAndDeletedAtIsNullAndCreatedAtBefore(Instant expiration);
}
