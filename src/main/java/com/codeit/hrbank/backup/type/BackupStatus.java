package com.codeit.hrbank.backup.type;

import lombok.Getter;

@Getter
public enum BackupStatus {
    IN_PROGRESS("진행중"),
    COMPLETED("완료"),
    SKIPPED("건너뜀"),
    FAILED("실패");

    private final String statusDisplayName;

    BackupStatus(String statusDisplayName) {
        this.statusDisplayName = statusDisplayName;
    }

}
