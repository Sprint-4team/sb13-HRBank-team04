package com.codeit.hrbank.changelog.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;

public record ChangeLogSearchCondition(

        @Schema(description = "대상 직원 사번")
        String employeeNumber,

        @Schema(description = "이력 유형 (ALL, CREATED, UPDATED, DELETED)")
        ChangeLogTypeFilter type,

        @Schema(description = "내용")
        String memo,

        @Schema(description = "IP 주소")
        String ipAddress,

        @Schema(description = "수정 일시(부터)")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant atFrom,

        @Schema(description = "수정 일시(까지)")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant atTo,

        @Schema(description = "이전 페이지 마지막 요소 ID")
        Long idAfter,

        @Schema(description = "커서 (이전 페이지의 마지막 ID)")
        String cursor,

        @Schema(description = "페이지 크기", defaultValue = "10")
        Integer size,

        @Schema(
                description = "정렬 필드 (ipAddress, at)",
                defaultValue = "at",
                allowableValues = {"ipAddress", "at"}
        )
        String sortField,

        @Schema(
                description = "정렬 방향 (asc, desc)",
                defaultValue = "desc",
                allowableValues = {"asc", "desc"}
        )
        String sortDirection
) {
}