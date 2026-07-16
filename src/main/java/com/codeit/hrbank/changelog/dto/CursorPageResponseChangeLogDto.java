package com.codeit.hrbank.changelog.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "커서 기반 페이지 응답")
public record CursorPageResponseChangeLogDto(

        @Schema(description = "페이지 내용")
        List<ChangeLogDto> content,

        @Schema(
                description = "다음 페이지 커서",
                example = "eyJpZCI6MjB9"
        )
        String nextCursor,

        @Schema(
                description = "마지막 요소의 ID",
                example = "20"
        )
        Long nextIdAfter,

        @Schema(
                description = "페이지 크기",
                example = "10"
        )
        Integer size,

        @Schema(
                description = "총 요소 수",
                example = "100"
        )
        Long totalElements,

        @Schema(
                description = "다음 페이지 여부",
                example = "true"
        )
        boolean hasNext

) {
}
