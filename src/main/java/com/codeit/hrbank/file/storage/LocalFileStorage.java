package com.codeit.hrbank.file.storage;

import com.codeit.hrbank.file.exception.FileNotFoundException;
import com.codeit.hrbank.file.exception.FileStorageException;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Component
@Slf4j
public class LocalFileStorage {
  /*
   파일 서비스는 비즈니스 로직만 담당하도록 분리
   디렉터리 존재 여부 확인 및 생성 등의 초기화 작업들은 storage에게 맡긴다
   */

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
      throw new FileStorageException("저장 경로가 업로드 디렉터리 외부입니다. path: "+uploadPath); // 커스텀 예외로 처리
    }
  }

  // 비즈니스 로직이 아닌 처리
  public StoredFile store(MultipartFile file, String originalFileName) {
    int dotIndex = originalFileName.lastIndexOf('.');
    String extension = (dotIndex >= 0) ? originalFileName.substring(dotIndex).toLowerCase() : "";

    // 충돌, 추측할 수 없는 UUID 파일명 + 원본 확장자
    String storedFileName = UUID.randomUUID().toString().replace("-", "") + extension;
    Path target = uploadPath.resolve(storedFileName).normalize();
    // 흐름: storedFileName -> resolve() -> targetPath -> DB 저장
    if (!target.startsWith(uploadPath)){
      throw new FileStorageException("저장 경로가 업로드 디렉터리 외부입니다. path: "+target);
    }
    try {
      // 디스크에 파일 저장
      file.transferTo(target);
    } catch (IOException e) {
      throw new FileStorageException("파일을 저장하는 데 실패하였습니다. file name: "+originalFileName);
    }
    return new StoredFile(storedFileName, target);
  }

  public Resource load(String storedPath) {
    Path requested = Paths.get(storedPath).normalize(); // DB -> path -> 파일 접근
    if (!requested.startsWith(uploadPath)) {
      log.warn("다운로드 요청 거부: {}", storedPath);
      throw new FileStorageException("저장 경로가 업로드 디렉터리 외부입니다. path: " + storedPath);
    }
    if (!Files.exists(requested) || Files.isDirectory(requested)){
      throw new FileNotFoundException("파일을 찾을 수 없습니다. path: "+ storedPath);
    }
    try {
      return new UrlResource(requested.toUri());
    } catch (MalformedURLException e) {
      throw new FileStorageException("파일 경로를 Resource로 변환하는 데 실패했습니다. path: "+requested);
    }
  }

  public void delete(String storedPath) {
    Path target = Paths.get(storedPath).normalize(); // 절대경로로 통일
    if (!target.startsWith(uploadPath)){
      log.warn("삭제 요청 거부: {}", storedPath);
      throw new FileStorageException("저장 경로가 업로드 디렉터리 외부입니다. path: "+ target);
    }
    try {
      Files.deleteIfExists(target);
    } catch (IOException e) {
      throw new FileStorageException("디스크 파일 삭제에 실패했습니다. path: "+storedPath);
    }
  }

  public record StoredFile(String storedFileName, Path target) { }
  // LocalFileStorage의 저장 결과를 전달하기 위한 것으로 Storage 외부에서는 재사용하지 않기 때문에 클래스 내부에 선언
}
