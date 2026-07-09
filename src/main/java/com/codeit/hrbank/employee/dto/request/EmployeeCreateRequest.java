package com.codeit.hrbank.employee.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record EmployeeCreateRequest(
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
    String memo

) {

}
