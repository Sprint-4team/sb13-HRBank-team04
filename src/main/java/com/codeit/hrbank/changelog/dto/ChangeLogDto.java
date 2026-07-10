package com.codeit.hrbank.changelog.dto;

import com.codeit.hrbank.changelog.EmployeeChangeType;

import java.time.Instant;

public record ChangeLogDto(
        Long id,
        EmployeeChangeType type,
        String employeeNumber,
        String memo,
        String ipAddress,
        Instant at
) {
}