package com.codeit.hrbank.department.exception;

import com.codeit.hrbank.common.BusinessException;
import org.springframework.http.HttpStatus;

public class DepartmentNotFoundException extends BusinessException {
    public DepartmentNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "부서를 찾을 수 없습니다. id=" + id);
    }
}