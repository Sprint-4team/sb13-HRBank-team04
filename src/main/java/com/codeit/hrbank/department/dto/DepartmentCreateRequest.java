package com.codeit.hrbank.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record DepartmentCreateRequest(
        @NotBlank(message = "부서명은 필수입니다.")
        String name,

        @NotBlank(message = "설명은 필수입니다.")
        String description,

        @NotNull(message = "설립일은 필수입니다.")
        LocalDate establishedDate
) {}