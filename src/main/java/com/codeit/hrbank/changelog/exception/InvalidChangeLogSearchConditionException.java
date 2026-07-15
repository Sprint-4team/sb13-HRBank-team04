package com.codeit.hrbank.changelog.exception;

import com.codeit.hrbank.common.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidChangeLogSearchConditionException
        extends BusinessException {

    public InvalidChangeLogSearchConditionException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
