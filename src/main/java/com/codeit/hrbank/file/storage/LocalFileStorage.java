package com.codeit.hrbank.file.storage;

import com.codeit.hrbank.file.exception.FileStorageException;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LocalFileStorage {
  // 파일 서비스는 비즈니스 로직만 담당하도록 분리

  private final Path uploadPath;

  public LocalFileStorage(@Value("${file.upload-dir}") String uploadDir) {
    this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
    // 파일 저장 경로는 백업과 협의하여 "절대 경로" 로 통일함
  }

  @PostConstruct
  public void init() {
    try {
      Files.createDirectories(uploadPath);
    } catch (IOException e) {
      throw new FileStorageException(uploadPath); // 커스텀 예외로 처리
    }
  }

  public Path getUploadPath() {
    return uploadPath;
  }

}
