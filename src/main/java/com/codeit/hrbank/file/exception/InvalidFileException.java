package com.codeit.hrbank.file.exception;

import com.codeit.hrbank.common.BusinessException;
import org.springframework.http.HttpStatus;

// 사용자의 잘못된 요청으로 빈 파일에 대한 예외 처리(400)
public class InvalidFileException extends BusinessException {

  public InvalidFileException() {
    super(HttpStatus.BAD_REQUEST, "빈 파일은 업로드할 수 없습니다.");
  }
}
