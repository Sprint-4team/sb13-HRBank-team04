package com.codeit.hrbank.backup.controller;

import com.codeit.hrbank.backup.dto.request.BackupSearchCondition;
import com.codeit.hrbank.backup.dto.response.BackupDto;
import com.codeit.hrbank.backup.dto.response.CursorPageResponseBackupDto;
import com.codeit.hrbank.backup.service.BackupHistoryService;
import com.codeit.hrbank.backup.type.BackupStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/backups")
public class BackupHistoryController {

    private final BackupHistoryService backupHistoryService;

    @PostMapping
    public ResponseEntity<BackupDto> createBackupHistory(HttpServletRequest request) {
        String worker = request.getRemoteAddr();

        BackupDto backupDto = backupHistoryService.createBackupHistory(worker);

        return ResponseEntity.ok(backupDto);
    }

    @GetMapping
    public ResponseEntity<CursorPageResponseBackupDto> findBackupHistories(
            @RequestParam(required = false) String worker,
            @RequestParam(required = false) BackupStatus status,
            @RequestParam(required = false) Instant startedAtFrom,
            @RequestParam(required = false) Instant startedAtTo,
            @RequestParam(required = false) Long idAfter,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startedAt") String sortField,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDirection) {

        BackupSearchCondition condition = new BackupSearchCondition(
                worker, status, startedAtFrom, startedAtTo, idAfter, cursor, size, sortField, sortDirection);

        CursorPageResponseBackupDto backupHistories = backupHistoryService.findBackupHistories(condition);

        return ResponseEntity.ok(backupHistories);
    }

    @GetMapping("/latest")
    public ResponseEntity<BackupDto> findLatestBackupHistory(@RequestParam(defaultValue = "COMPLETED") BackupStatus status) {
        BackupDto backupDto = backupHistoryService.findLatestBackupHistory(status);

        return ResponseEntity.ok(backupDto);
    }

}
