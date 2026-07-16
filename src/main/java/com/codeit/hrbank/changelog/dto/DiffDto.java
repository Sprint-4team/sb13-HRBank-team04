package com.codeit.hrbank.changelog.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "직원 정보 수정 이력 변경 내용 (상세 조회용)")
public record DiffDto(

        @Schema(
                description = "속성 이름",
                example = "직함"
        )
        String propertyName,

        @Schema(
                description = "수정 전 값",
                example = "사원"
        )
        String before,

        @Schema(
                description = "수정 후 값",
                example = "대리"
        )
        String after

) {
}