package com.codeit.hrbank.changelog.exception;

import com.codeit.hrbank.common.BusinessException;
import org.springframework.http.HttpStatus;

public class ChangeLogNotFoundException extends BusinessException {

    public ChangeLogNotFoundException(Long id) {
        super(
                HttpStatus.NOT_FOUND,
                "직원 변경 이력을 찾을 수 없습니다: " + id
        );
    }
}
