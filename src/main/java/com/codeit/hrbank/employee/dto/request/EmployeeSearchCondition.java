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
    Long idAfter,            // 커서 페이지네이션용
    String cursor,
    int size,
    String sortField,        // 정렬 기준 (name/hireDate/employeeNumber 중 1개)
    Sort.Direction sortDirection
) {}