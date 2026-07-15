package com.codeit.hrbank.backup.exception;

import com.codeit.hrbank.common.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidBackupSearchConditionException extends BusinessException {

    public InvalidBackupSearchConditionException(String details) {
        super(HttpStatus.BAD_REQUEST, details);
    }
}
