package com.codeit.hrbank.backup.exception;

import com.codeit.hrbank.common.BusinessException;
import org.springframework.http.HttpStatus;

public class BackupAlreadyInProgressException extends BusinessException {
    public BackupAlreadyInProgressException() {
        super(HttpStatus.CONFLICT, "이미 진행 중인 백업이 존재합니다.");
    }
}
