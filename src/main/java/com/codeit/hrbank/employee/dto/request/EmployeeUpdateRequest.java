package com.codeit.hrbank.employee.dto.request;

import com.codeit.hrbank.employee.enums.EmployeeStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record EmployeeUpdateRequest(
    @NotBlank
    String name,
    @NotBlank
    @Email
    String email,
    @NotNull
    Long departmentId,
    @NotBlank
    String position,
    @NotNull
    LocalDate hireDate,
    @NotNull
    EmployeeStatus status,
    String memo
) {

}
