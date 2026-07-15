package com.codeit.hrbank.file.controller;

import com.codeit.hrbank.file.entity.File;
import com.codeit.hrbank.file.service.FileService;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileController {
  // 요청, 응답만 담당

  private final FileService fileService;

  // 다운로드 API - 백업, 프로필, 로그
  @GetMapping("/{id}/download")
  public ResponseEntity<Resource> downloadFile(@PathVariable("id") Long id) {
    File file = fileService.findFile(id); // 지역변수로 선언해 처리
    Resource resource = fileService.downloadFile(id);
    String encodedFileName = UriUtils.encode(file.getOriginalFileName(), StandardCharsets.UTF_8);
    // 인코딩, 한글 깨짐 방지

    return ResponseEntity
        .status(HttpStatus.OK)
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename*=UTF-8''"
                +encodedFileName
        ) // 파일명이 한글이라도 깨지지 않고 출력
        .contentType(MediaType.parseMediaType(file.getContentType()))
        .body(resource);
  }
}