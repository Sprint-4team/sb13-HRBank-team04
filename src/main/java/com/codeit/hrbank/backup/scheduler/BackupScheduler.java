package com.codeit.hrbank.backup.scheduler;

import com.codeit.hrbank.backup.service.BackupHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BackupScheduler {

    private final BackupHistoryService backupHistoryService;

    @Scheduled(fixedDelayString = "${backup.schedule.interval}")
    public void autoBackup() {
        log.info("자동 백업 시작");

        backupHistoryService.createBackupHistory("system");

        log.info("자동 백업 종료");
    }
}
