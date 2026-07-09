package com.codeit.hrbank.backup.repository;

import com.codeit.hrbank.backup.entity.BackupHistory;
import com.codeit.hrbank.backup.type.BackupStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BackupHistoryRepository extends JpaRepository<BackupHistory, Long> {

    // 파라미터로 받은 status 상태의 가장 최근 백업 정보 조회
    Optional<BackupHistory> findTopByStatusOrderByStartedAtDesc(BackupStatus status);

}
