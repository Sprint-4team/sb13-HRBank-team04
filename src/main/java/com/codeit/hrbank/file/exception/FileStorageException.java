package com.codeit.hrbank.file.exception;

import com.codeit.hrbank.common.BusinessException;
import java.nio.file.Path;
import org.springframework.http.HttpStatus;

public class FileStorageException extends BusinessException {
  // 파일 작업 관련 시스템 오류(500)
  public FileStorageException(Long id) {
    super(HttpStatus.INTERNAL_SERVER_ERROR, "파일 경로가 유효하지 않습니다.: "+id);
  }
  public FileStorageException(Path path){
    super(HttpStatus.INTERNAL_SERVER_ERROR, "저장 경로가 업로드 디렉터리 외부입니다.: "+path);
  }
  public FileStorageException(String originalFileName){
    super(HttpStatus.INTERNAL_SERVER_ERROR, "파일을 저장하는 데 실패하였습니다.: "+originalFileName);
  }

}
