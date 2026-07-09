package com.codeit.hrbank.backup.dto.request;

import com.codeit.hrbank.backup.type.BackupStatus;
import org.springframework.data.domain.Sort;

import java.time.Instant;

public record BackupSearchCondition(
        String worker,
        BackupStatus status,
        Instant startedAtFrom,
        Instant startedAtTo,
        Long idAfter,
        String cursor,
        int size,
        String sortField,
        Sort.Direction sortDirection
) {
}
