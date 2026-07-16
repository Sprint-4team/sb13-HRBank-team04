package com.codeit.hrbank.file.service;

import com.codeit.hrbank.file.dto.FileDownloadDto;
import com.codeit.hrbank.file.exception.FileNotFoundException;
import com.codeit.hrbank.file.entity.File;
import com.codeit.hrbank.file.exception.InvalidFileException;
import com.codeit.hrbank.file.repository.FileRepository;
import com.codeit.hrbank.file.storage.LocalFileStorage;
import com.codeit.hrbank.file.storage.LocalFileStorage.StoredFile;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
  // 비즈니스 로직 처리

  private final FileRepository fileRepository;
  private final LocalFileStorage localFileStorage;

  // 직원 프로필 저장
  @Override
  @Transactional
  public File createFile(MultipartFile file){
    if (file == null || file.isEmpty()) {
      throw new InvalidFileException(); // 빈 파일은 사용자의 잘못된 요청이므로 분리해 처리
    }

    // 원본 파일명 정규화한 뒤 확장자 추출
    String originalFileName = StringUtils.cleanPath(
        file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename());

    StoredFile storedFile = localFileStorage.store(file, originalFileName);
    // DB에 메타 정보 저장
      File fileEntity = new File(
          originalFileName,
          storedFile.storedFileName(),
          file.getContentType() != null ? file.getContentType()
          : MediaType.APPLICATION_OCTET_STREAM_VALUE, // 타입 null 일 때 기본값으로 처리
          file.getSize(),
          storedFile.target().toString() // 다운로드 위한 경로(path) 저장
          );
      try {
        return fileRepository.save(fileEntity);
      } catch (RuntimeException e) {
        throw new RuntimeException(e);
      }
  }

  @Override
  public File findFile(Long id) {
    return fileRepository.findById(id).orElseThrow(
        ()->new FileNotFoundException("파일을 찾을 수 없습니다. id = "+ id)
    );
  }

  // id -> 파일 다운로드
  @Override
  public FileDownloadDto downloadFile(Long id) {
    File file = findFile(id);
    Resource resource = localFileStorage.load(file.getPath());
    return new FileDownloadDto(resource, file.getOriginalFileName(), file.getContentType());
  }

  @Override
  @Transactional
  public void deleteFile(Long id) {
    File file = findFile(id);
    localFileStorage.delete(file.getPath()); // 디스크 삭제
    fileRepository.delete(file); // DB 삭제 - 설령 실패하더라도 메타데이터를 관리하는 편이 쉽기 때문에 디스크 삭제 우선
  }
}
