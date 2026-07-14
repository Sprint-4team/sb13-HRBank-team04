package com.codeit.hrbank.backup.dto.request;

import com.codeit.hrbank.backup.type.BackupStatus;
import java.time.Instant;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;

public record BackupSearchCondition(
        String worker,
        BackupStatus status,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant startedAtFrom,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant startedAtTo,
        Long idAfter,
        String cursor,
        Integer size,
        String sortField,
        Sort.Direction sortDirection
) {
}
