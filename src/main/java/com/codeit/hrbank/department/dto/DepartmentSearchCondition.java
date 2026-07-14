package com.codeit.hrbank.department.dto;

import org.springframework.data.domain.Sort;

public record DepartmentSearchCondition(
        String nameOrDescription,
        Long idAfter,
        String cursor,
        int size,
        String sortField,
        Sort.Direction sortDirection
) {
}