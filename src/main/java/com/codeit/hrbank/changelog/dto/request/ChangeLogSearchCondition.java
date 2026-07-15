package com.codeit.hrbank.changelog.dto.request;

import com.codeit.hrbank.changelog.EmployeeChangeType;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;

public record ChangeLogSearchCondition(

        @Schema(description = "대상 직원 사번")
        String employeeNumber,

        @Schema(
                description = "이력 유형",
                allowableValues = {"CREATED", "UPDATED", "DELETED"}
        )
        EmployeeChangeType type,

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

        @Schema(description = "커서")
        String cursor,

        @Schema(description = "페이지 크기", defaultValue = "10")
        Integer size,

        @Schema(
                description = "정렬 필드",
                defaultValue = "at",
                allowableValues = {"ipAddress", "at"}
        )
        String sortField,

        @Schema(
                description = "정렬 방향",
                defaultValue = "desc",
                allowableValues = {"asc", "desc"}
        )
        String sortDirection
) {
}