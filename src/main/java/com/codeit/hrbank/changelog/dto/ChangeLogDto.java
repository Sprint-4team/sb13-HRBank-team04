package com.codeit.hrbank.changelog.dto;

import com.codeit.hrbank.changelog.EmployeeChangeType;import java.time.LocalDateTime;

public record ChangeLogDto(
        Long id,
        EmployeeChangeType type,
        String employeeNumber,
        String memo,
        String ipAddress,
        LocalDateTime at
) {
}