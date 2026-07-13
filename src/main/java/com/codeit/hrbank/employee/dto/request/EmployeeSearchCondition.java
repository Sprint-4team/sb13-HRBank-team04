package com.codeit.hrbank.employee.dto.request;

import com.codeit.hrbank.employee.enums.EmployeeStatus;
import java.time.LocalDate;
import org.springframework.data.domain.Sort;

public record EmployeeSearchCondition(
    String nameOrEmail,
    String departmentName,
    String position,
    String employeeNumber,
    LocalDate hireDateFrom,
    LocalDate hireDateTo,
    EmployeeStatus status,
    String cursor,        // 마지막 요소의 정렬 기준값 (문자열로 전달받음)
    Long idAfter,          // 마지막 요소의 id (tie-breaker)
    int size,
    String sortField,      // name / hireDate / employeeNumber
    Sort.Direction sortDirection
) {}