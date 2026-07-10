package com.codeit.hrbank.file.service;

import com.codeit.hrbank.file.entity.File;
import com.codeit.hrbank.file.repository.FileRepository;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class FileService {
  // 비즈니스 로직 처리

  private final FileRepository fileRepository;
  private final Path uploadPath;

  public FileService(FileRepository fileRepository, @Value("${file.upload-dir}") String uploadDir){
    this.fileRepository = fileRepository;
    // 저장 경로는 절대 경로로 정규화
    this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
    try {
      Files.createDirectories(this.uploadPath);
    } catch (IOException e) {
      throw new RuntimeException("업로드 디렉터리 생성 실패: " + e); // 커스텀 예외로 처리할 수도 있는 부분
    }
  }

  // 파일 메타 정보 -> 데이터베이스, 실제 파일 -> 로컬 디스크에 저장
  @Transactional
  public File createFile(MultipartFile file){
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("파일 비어 있음");
    }

    // 원본 파일명 정규화한 뒤 확장자 추출
    String originalFileName = StringUtils.cleanPath(
        file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename());

    int dotIndex = originalFileName.lastIndexOf('.');
    String extension = (dotIndex >= 0) ? originalFileName.substring(dotIndex).toLowerCase() : "";

    // 충돌, 추측할 수 없는 UUID 파일명 + 원본 확장자
    String storedFileName = UUID.randomUUID().toString().replace("-", "") + extension;
    Path target = uploadPath.resolve(storedFileName).normalize();
    // 흐름: storedFileName -> resolve() -> targetPath -> DB 저장
    if (!target.startsWith(uploadPath)){
      throw new IllegalArgumentException("파일을 찾을 수 없음");
    }
    try {
      // 디스크에 파일 저장
      file.transferTo(target);
      // DB에 메타 정보 저장
      File createdFile = new File(
          originalFileName,
          storedFileName,
          file.getContentType(),
          file.getSize(),
          target.toString() // 다운로드 위한 경로(path) 저장
          );
      return fileRepository.save(createdFile);
    } catch (IOException e) {
      throw new RuntimeException("파일 저장 실패: " + e);
    }
  }

  public File findFile(Long id) {
    return fileRepository.findById(id).orElseThrow(
        ()->new NoSuchElementException("파일을 찾을 수 없음")
    );
  }

  // id -> 파일 다운로드
  public Resource downloadFile(Long id) {
    File file = findFile(id);
    Path requested = Paths.get(file.getPath()).normalize(); // DB -> path -> 파일 접근
    if (!requested.startsWith(uploadPath)){
      log.warn("다운로드 요청 거부");
    }
    if (!Files.exists(requested) || Files.isDirectory(requested)){
      throw new NoSuchElementException("파일을 찾을 수 없음");
    }
    try {
      return new UrlResource(requested.toUri());
    } catch (MalformedURLException e) {
      throw new RuntimeException("파일 경로 잘못됨" + e);
    }
  }

  @Transactional
  public void deleteFile(Long id) {
    File file = findFile(id);
    Path target = Paths.get(file.getPath()).normalize();
    if (!target.startsWith(uploadPath)){
      log.warn("삭제 요청 거부"); // 에러 직접 던지지 않고 로그로 기록해 조용히 처리
    }
    try {
      Files.deleteIfExists(target);
    } catch (IOException e) {
      log.error("파일 삭제 실패: " + e);
    }
    fileRepository.delete(file); // 메타데이터 삭제
  }
}
