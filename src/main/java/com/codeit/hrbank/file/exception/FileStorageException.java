package com.codeit.hrbank.file.exception;

import com.codeit.hrbank.common.BusinessException;
import java.nio.file.Path;
import org.springframework.http.HttpStatus;

public class FileStorageException extends BusinessException {
  // 파일 작업 관련 시스템 오류(500)
  public FileStorageException(Long id) {
    super(HttpStatus.INTERNAL_SERVER_ERROR, "생성 실패: "+id);
  }
  public FileStorageException(Path path){
    super(HttpStatus.INTERNAL_SERVER_ERROR, "생성 실패: "+path);
  }
  public FileStorageException(String originalFileName){
    super(HttpStatus.INTERNAL_SERVER_ERROR, "생성 실패: "+originalFileName);
  }

}
