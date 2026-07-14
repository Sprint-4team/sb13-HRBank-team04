package com.codeit.hrbank.department.dto;

import java.util.List;

public record CursorPageResponseDepartmentDto(
        List<DepartmentDto> content,
        String nextCursor,
        Long nextIdAfter,
        Integer size,
        Long totalElements,
        boolean hasNext
) {
}