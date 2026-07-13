package com.codeit.hrbank.backup.controller;

import com.codeit.hrbank.backup.dto.request.BackupSearchCondition;
import com.codeit.hrbank.backup.dto.response.BackupDto;
import com.codeit.hrbank.backup.dto.response.CursorPageResponseBackupDto;
import com.codeit.hrbank.backup.service.BackupHistoryService;
import com.codeit.hrbank.backup.type.BackupStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
public class BackupHistoryController {

    private final BackupHistoryService backupHistoryService;

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
