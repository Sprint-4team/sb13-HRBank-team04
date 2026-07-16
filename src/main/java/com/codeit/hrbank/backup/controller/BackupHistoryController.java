package com.codeit.hrbank.backup.controller;

import com.codeit.hrbank.backup.dto.request.BackupSearchCondition;
import com.codeit.hrbank.backup.dto.response.BackupDto;
import com.codeit.hrbank.backup.dto.response.CursorPageResponseBackupDto;
import com.codeit.hrbank.backup.service.BackupHistoryService;
import com.codeit.hrbank.backup.type.BackupStatus;
import com.codeit.hrbank.common.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/backups")
@Tag(name = "데이터 백업 관리", description = "데이터 백업 관리 API")
public class BackupHistoryController {

    private final BackupHistoryService backupHistoryService;

    @Operation(summary = "데이터 백업 생성", description = "데이터 백업을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "백업 생성 성공", content = @Content(
                    schema = @Schema(implementation = BackupDto.class)
            )),
            @ApiResponse(responseCode = "409", description = "이미 진행 중인 백업이 있음", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            ))
    })
    @PostMapping
    public ResponseEntity<BackupDto> createBackupHistory(HttpServletRequest request) {
        String worker = request.getRemoteAddr();

        BackupDto backupDto = backupHistoryService.createBackupHistory(worker);

        return ResponseEntity.status(HttpStatus.OK).body(backupDto);
    }

    // 데이터 백업 목록 조회
    @GetMapping
    @Operation(summary = "백업 이력 목록 조회", description = "검색 조건에 맞는 백업 이력을 커서 기반으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(
                    schema = @Schema(implementation = CursorPageResponseBackupDto.class)
            )),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 지원하지 않는 정렬 필드", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            ))
    })
    public ResponseEntity<CursorPageResponseBackupDto> findBackupHistories(
            @ModelAttribute BackupSearchCondition condition) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(backupHistoryService.findBackupHistories(condition));
    }

    // 마지막 백업 조회
    @GetMapping("/latest")
    @Operation(summary = "최근 백업 이력 조회", description = "지정한 상태의 가장 최근 백업 이력을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(
                    schema = @Schema(implementation = BackupDto.class)
            )),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 유효하지 않은 백업 상태", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            ))
    })
    public ResponseEntity<BackupDto> findLatestBackupHistory(
            @Parameter(description = "백업 상태")
            @RequestParam(name = "status", defaultValue = "COMPLETED") BackupStatus status) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(backupHistoryService.findLatestBackupHistory(status));
    }
}
