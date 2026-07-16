package com.codeit.hrbank.file.exception;

import com.codeit.hrbank.common.BusinessException;
import org.springframework.http.HttpStatus;

public class FileStorageException extends BusinessException {
  /*
    파일 작업 관련 시스템 오류(500)
    예외 클래스 단순화: 하나의 예외가 아니기 때문에 매개변수 타입으로 구분하지 않고,
    각각 예외가 발생하는 곳에서 다른 메시지를 전달하도록 한다
   */
  public FileStorageException(String message) {
    super(HttpStatus.INTERNAL_SERVER_ERROR, message);
  }
}
