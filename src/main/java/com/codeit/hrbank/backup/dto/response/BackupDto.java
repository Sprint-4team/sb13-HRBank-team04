package com.codeit.hrbank.backup.dto.response;

import com.codeit.hrbank.backup.entity.BackupHistory;
import com.codeit.hrbank.backup.type.BackupStatus;

import java.time.Instant;

public record BackupDto(
        Long id,
        String worker,
        Instant startedAt,
        Instant endedAt,
        BackupStatus status
//        Long fileId
) {
    public static BackupDto from(BackupHistory history) {
        return new BackupDto(
                history.getId(),
                history.getWorker(),
                history.getStartedAt(),
                history.getEndedAt(),
                history.getStatus()
//                history.getFile().getId()
        );
    }
}
