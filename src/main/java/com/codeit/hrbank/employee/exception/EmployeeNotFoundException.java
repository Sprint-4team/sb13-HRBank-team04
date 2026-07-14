package com.codeit.hrbank.employee.exception;

import com.codeit.hrbank.common.BusinessException;
import org.springframework.http.HttpStatus;

public class EmployeeNotFoundException extends BusinessException {

  public EmployeeNotFoundException(Long id) {
    super(HttpStatus.NOT_FOUND, "직원을 찾을 수 없습니다: " + id);
  }
}
