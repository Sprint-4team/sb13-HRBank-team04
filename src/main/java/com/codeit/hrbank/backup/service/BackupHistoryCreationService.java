package com.codeit.hrbank.backup.service;

import com.codeit.hrbank.backup.entity.BackupHistory;
import com.codeit.hrbank.backup.repository.BackupHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BackupHistoryCreationService {

    private final BackupHistoryRepository backupHistoryRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long createInProgress(String worker) {
        BackupHistory backupHistory = BackupHistory.inProgress(worker);

        // 백업 이력 저장
        backupHistory = backupHistoryRepository.save(backupHistory);

        return backupHistory.getId();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long createSkipped(String worker) {
        BackupHistory backupHistory = BackupHistory.skipped(worker);

        // 백업 이력 저장
        backupHistory = backupHistoryRepository.save(backupHistory);

        return backupHistory.getId();
    }

}
