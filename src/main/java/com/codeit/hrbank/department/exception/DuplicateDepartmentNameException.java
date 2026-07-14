package com.codeit.hrbank.department.exception;

import com.codeit.hrbank.common.BusinessException;
import org.springframework.http.HttpStatus;

public class DuplicateDepartmentNameException extends BusinessException {
    public DuplicateDepartmentNameException(String name) {
        super(HttpStatus.BAD_REQUEST, "이미 존재하는 부서 이름입니다: " + name);
    }
}