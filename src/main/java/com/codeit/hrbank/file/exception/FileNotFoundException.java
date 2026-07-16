package com.codeit.hrbank.file.exception;

import com.codeit.hrbank.common.BusinessException;
import org.springframework.http.HttpStatus;

public class FileNotFoundException extends BusinessException {
  /*
  존재하지 않는 파일 오류 처리(404)
  예외 클래스 단순화: 하나의 예외가 아니기 때문에 매개변수 타입으로 구분하지 않고,
  각각 예외가 발생하는 곳에서 다른 메시지를 전달하도록 한다
   */
  public FileNotFoundException(String message) {
    super(HttpStatus.NOT_FOUND, message);
  }
}
