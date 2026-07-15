package com.codeit.hrbank.employee.dto;

public record EmployeeTrendDto(
    String date,
    Long count,
    Long change,
    Double changeRate
) {
}