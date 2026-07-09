package com.codeit.hrbank.changelog.dto;

import java.util.List;

public record CursorPageResponseChangeLogDto(
        List<ChangeLogDto> content,
        String nextCursor,
        Long nextIdAfter,
        Integer size,
        Long totalElements,
        boolean hasNext
) {
}
