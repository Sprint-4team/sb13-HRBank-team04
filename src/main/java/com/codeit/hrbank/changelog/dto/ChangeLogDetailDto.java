package com.codeit.hrbank.changelog.dto;

import com.codeit.hrbank.changelog.EmployeeChangeType;

import java.time.Instant;
import java.util.List;

public record ChangeLogDetailDto(
        Long id,
        EmployeeChangeType type,
        String employeeNumber,
        String memo,
        String ipAddress,
        Instant at,
        String employeeName,
        Long profileImageId,
        List<DiffDto> diffs
) {
}
