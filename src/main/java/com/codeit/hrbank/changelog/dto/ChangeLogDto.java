package com.codeit.hrbank.changelog.dto;

import com.codeit.hrbank.changelog.EmployeeChangeType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "직원 정보 수정 이력 (목록 조회용)")
public record ChangeLogDto(

        @Schema(
                description = "이력 ID",
                example = "1"
        )
        Long id,

        @Schema(
                description = "유형 (직원 추가, 정보 수정, 직원 삭제)",
                example = "UPDATED"
        )
        EmployeeChangeType type,

        @Schema(
                description = "직원 사번",
                example = "EMP-2023-001"
        )
        String employeeNumber,

        @Schema(
                description = "내용",
                example = "직함 변경에 따른 수정"
        )
        String memo,

        @Schema(
                description = "IP 주소",
                example = "192.168.0.1"
        )
        String ipAddress,

        @Schema(
                description = "수정 일시",
                example = "2023-01-01T12:00:00Z"
        )
        Instant at

) {
}