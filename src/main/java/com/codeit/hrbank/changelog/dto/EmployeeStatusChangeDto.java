package com.codeit.hrbank.changelog.dto;

import com.codeit.hrbank.employee.enums.EmployeeStatus;

import java.time.Instant;

public record EmployeeStatusChangeDto(
        Long employeeId,
        String employeeNumber,
        EmployeeStatus beforeStatus,
        EmployeeStatus afterStatus,
        Instant changedAt
) {
}