package com.codeit.hrbank.backup.exception;

import com.codeit.hrbank.common.BusinessException;
import org.springframework.http.HttpStatus;

public class BackupHistoryNotFoundException extends BusinessException {
    public BackupHistoryNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "백업 이력을 찾을 수 없습니다. id=" + id);
    }
}
