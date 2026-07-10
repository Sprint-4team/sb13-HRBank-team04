package com.codeit.hrbank.backup.controller;

import com.codeit.hrbank.backup.dto.request.BackupSearchCondition;
import com.codeit.hrbank.backup.dto.response.BackupDto;
import com.codeit.hrbank.backup.dto.response.CursorPageResponseBackupDto;
import com.codeit.hrbank.backup.service.BackupHistoryService;
import com.codeit.hrbank.backup.type.BackupStatus;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping
    public ResponseEntity<CursorPageResponseBackupDto> findBackupHistories(
            @RequestParam(required = false) String worker,
            @RequestParam(required = false) BackupStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startedAtFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startedAtTo,
            @RequestParam(required = false) Long idAfter,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startedAt") String sortField,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDirection) {

        BackupSearchCondition condition = new BackupSearchCondition(
                worker, status, startedAtFrom, startedAtTo, idAfter, cursor, size, sortField, sortDirection);

        backupHistoryService.findBackupHistories(condition);

        return ResponseEntity.ok(null);
    }

    @GetMapping("/latest")
    public ResponseEntity<BackupDto> findLatestBackupHistory(@RequestParam(defaultValue = "COMPLETED") BackupStatus status) {
        BackupDto backupDto = backupHistoryService.findLatestBackupHistory(status);

        return ResponseEntity.ok(backupDto);
    }

}
