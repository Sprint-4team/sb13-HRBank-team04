package com.codeit.hrbank.employee.dto;

public record EmployeeDistributionDto(
    String groupKey,
    Long count,
    Double percentage
) {

}
