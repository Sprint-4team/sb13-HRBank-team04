package com.codeit.hrbank.file.controller;

import com.codeit.hrbank.file.service.FileService;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileController {
  // 요청, 응답만 담당

  private final FileService fileService;

  @GetMapping("/{id}/download")
  public ResponseEntity<Resource> downloadFile(@PathVariable("id") Long id) {
    return ResponseEntity
        .status(HttpStatus.OK)
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\""+fileService.findFile(id).getOriginalFileName()+"\""
        )
        .contentType(MediaType.parseMediaType(fileService.findFile(id).getContentType()))
        .body(fileService.downloadFile(id));
  }
}
