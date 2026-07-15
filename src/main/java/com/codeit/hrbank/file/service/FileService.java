package com.codeit.hrbank.file.service;

import com.codeit.hrbank.file.dto.FileDownloadDto;
import com.codeit.hrbank.file.entity.File;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
  File createFile(MultipartFile file);
  File findFile(Long id);
  FileDownloadDto downloadFile(Long id);
  void deleteFile(Long id);
}
