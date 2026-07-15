package com.codeit.hrbank.backup.controller;

import com.codeit.hrbank.backup.dto.request.BackupSearchCondition;
import com.codeit.hrbank.backup.dto.response.BackupDto;
import com.codeit.hrbank.backup.dto.response.CursorPageResponseBackupDto;
import com.codeit.hrbank.backup.service.BackupHistoryService;
import com.codeit.hrbank.backup.type.BackupStatus;
import com.codeit.hrbank.common.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
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
    public ResponseEntity<CursorPageResponseBackupDto> findBackupHistories(
            @ModelAttribute BackupSearchCondition condition) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(backupHistoryService.findBackupHistories(condition));
    }

    // 마지막 백업 조회
    @GetMapping("/latest")
    public ResponseEntity<BackupDto> findLatestBackupHistory(
            @RequestParam(name = "status", defaultValue = "COMPLETED") BackupStatus status) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(backupHistoryService.findLatestBackupHistory(status));
    }
}
