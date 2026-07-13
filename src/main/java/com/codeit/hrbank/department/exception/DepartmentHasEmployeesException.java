package com.codeit.hrbank.department.exception;

import com.codeit.hrbank.common.BusinessException;
import org.springframework.http.HttpStatus;

public class DepartmentHasEmployeesException extends BusinessException {
    public DepartmentHasEmployeesException(Long id) {
        super(HttpStatus.BAD_REQUEST, "소속된 직원이 있어 부서를 삭제할 수 없습니다: " + id);
    }
}