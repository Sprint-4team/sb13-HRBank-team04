package com.codeit.hrbank.backup.repository;

import com.codeit.hrbank.backup.entity.BackupHistory;
import com.codeit.hrbank.backup.type.BackupStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BackupHistoryRepository extends JpaRepository<BackupHistory, Long> {

    Optional<BackupHistory> findTopByStatusOrderByStartedAtDesc(BackupStatus status);
    boolean existsByStatus(BackupStatus status);

    @Query("""
            select b
            from BackupHistory b
            where (:worker is null or :worker = '' or lower(b.worker) like lower(concat('%', :worker, '%')))
              and (:status is null or b.status = :status)
              and b.startedAt >= coalesce(:startedAtFrom, b.startedAt)
              and b.startedAt <= coalesce(:startedAtTo, b.startedAt)
            order by b.startedAt desc, b.id desc
            """)
    List<BackupHistory> findHistories(
            @Param("worker") String worker,
            @Param("status") BackupStatus status,
            @Param("startedAtFrom") Instant startedAtFrom,
            @Param("startedAtTo") Instant startedAtTo,
            Pageable pageable
    );

    @Query("""
            select b
            from BackupHistory b
            where (:worker is null or :worker = '' or lower(b.worker) like lower(concat('%', :worker, '%')))
              and (:status is null or b.status = :status)
              and b.startedAt >= coalesce(:startedAtFrom, b.startedAt)
              and b.startedAt <= coalesce(:startedAtTo, b.startedAt)
              and (
                    b.startedAt < :cursorStartedAt
                    or (b.startedAt = :cursorStartedAt and b.id < :idAfter)
                  )
            order by b.startedAt desc, b.id desc
            """)
    List<BackupHistory> findHistoriesAfter(
            @Param("worker") String worker,
            @Param("status") BackupStatus status,
            @Param("startedAtFrom") Instant startedAtFrom,
            @Param("startedAtTo") Instant startedAtTo,
            @Param("cursorStartedAt") Instant cursorStartedAt,
            @Param("idAfter") Long idAfter,
            Pageable pageable
    );

    @Query("""
            select count(b)
            from BackupHistory b
            where (:worker is null or :worker = '' or lower(b.worker) like lower(concat('%', :worker, '%')))
              and (:status is null or b.status = :status)
              and b.startedAt >= coalesce(:startedAtFrom, b.startedAt)
              and b.startedAt <= coalesce(:startedAtTo, b.startedAt)
            """)
    long countHistories(
            @Param("worker") String worker,
            @Param("status") BackupStatus status,
            @Param("startedAtFrom") Instant startedAtFrom,
            @Param("startedAtTo") Instant startedAtTo
    );
}
