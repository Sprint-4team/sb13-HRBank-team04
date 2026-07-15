package com.codeit.hrbank.changelog.service;

import com.codeit.hrbank.changelog.EmployeeChangeType;
import com.codeit.hrbank.changelog.dto.ChangeLogDetailDto;
import com.codeit.hrbank.changelog.dto.CursorPageResponseChangeLogDto;
import com.codeit.hrbank.changelog.dto.DiffDto;
import com.codeit.hrbank.employee.entity.Employee;

import java.time.Instant;
import java.util.List;

public interface ChangeLogService {

    CursorPageResponseChangeLogDto findChangeLogs(
            String employeeNumber,
            EmployeeChangeType type,
            String memo,
            String ipAddress,
            Instant atFrom,
            Instant atTo,
            Long idAfter,
            String cursor,
            Integer size,
            String sortField,
            String sortDirection
    );

    ChangeLogDetailDto findChangeLogDetail(Long id);

    Long findChangeLogDetailCount(
            Instant fromDate,
            Instant toDate
    );

    void saveChangeLog(
            EmployeeChangeType type,
            Employee employee,
            String employeeNumber,
            String memo,
            String ipAddress,
            List<DiffDto> diffs
    );

    void clearEmployeeReference(Long employeeId);
}
