package com.codeit.hrbank.employee.exception;

import com.codeit.hrbank.common.BusinessException;
import org.springframework.http.HttpStatus;

public class EmailDuplicateException extends BusinessException {

  public EmailDuplicateException(String email) {
    super(HttpStatus.CONFLICT, "이미 사용중인 이메일입니다: " + email);
  }
}
