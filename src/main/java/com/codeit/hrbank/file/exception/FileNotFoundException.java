package com.codeit.hrbank.file.exception;

import com.codeit.hrbank.common.BusinessException;
import org.springframework.http.HttpStatus;

public class FileNotFoundException extends BusinessException {
  // 존재하지 않는 파일 오류 처리(404)
  public FileNotFoundException(Long id) {
    super(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다.: "+ id);
  }
  public FileNotFoundException() {
    super(HttpStatus.NOT_FOUND, "파일이 비어 있습니다.");
  }
}
