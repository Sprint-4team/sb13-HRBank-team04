package com.codeit.hrbank.changelog.controller;

import com.codeit.hrbank.changelog.dto.ChangeLogDetailDto;
import com.codeit.hrbank.changelog.dto.CursorPageResponseChangeLogDto;
import com.codeit.hrbank.changelog.dto.request.ChangeLogSearchCondition;
import com.codeit.hrbank.changelog.service.ChangeLogService;
import com.codeit.hrbank.common.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/change-logs")
@RequiredArgsConstructor
@Tag(
        name = "직원 정보 수정 이력 관리",
        description = "직원 정보 수정 이력 관리 API"
)
public class ChangeLogController {

    private final ChangeLogService changeLogService;

    @Operation(
            summary = "직원 정보 수정 이력 목록 조회",
            description = "직원 정보 수정 이력 목록을 조회합니다. 상세 변경 내용은 포함되지 않습니다.",
            operationId = "getAllChangeLogs"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            schema = @Schema(
                                    implementation = CursorPageResponseChangeLogDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 또는 지원하지 않는 정렬 필드",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping
    public CursorPageResponseChangeLogDto findChangeLogs(
            @ParameterObject
            @ModelAttribute ChangeLogSearchCondition condition
    ) {
        return changeLogService.findChangeLogs(condition);
    }

    @Operation(
            summary = "직원 정보 수정 이력 상세 조회",
            description = "직원 정보 수정 이력의 상세 정보를 조회합니다. 변경 상세 내용이 포함됩니다.",
            operationId = "getChangeLogDiffs"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ChangeLogDetailDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "이력을 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    public ChangeLogDetailDto findChangeLogDetail(
            @Parameter(
                    description = "이력 ID",
                    required = true
            )
            @PathVariable Long id
    ) {
        return changeLogService.findChangeLogDetail(id);
    }

    @Operation(
            summary = "수정 이력 건수 조회",
            description = "직원 정보 수정 이력 건수를 조회합니다. 파라미터를 제공하지 않으면 최근 일주일 데이터를 반환합니다.",
            operationId = "getChangeLogsCount"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            schema = @Schema(
                                    implementation = Long.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 또는 유효하지 않은 날짜 범위",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping("/count")
    public Long findChangeLogDetailCount(
            @Parameter(
                    description = "시작 일시 (기본값: 7일 전)"
            )
            @RequestParam(required = false)
            Instant fromDate,

            @Parameter(
                    description = "종료 일시 (기본값: 현재)"
            )
            @RequestParam(required = false)
            Instant toDate
    ) {
        return changeLogService.findChangeLogDetailCount(
                fromDate,
                toDate
        );
    }

}
