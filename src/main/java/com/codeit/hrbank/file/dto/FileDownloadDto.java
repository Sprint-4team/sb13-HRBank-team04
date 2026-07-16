package com.codeit.hrbank.file.dto;

import org.springframework.core.io.Resource;

// 파일 다운로드 응답 DTO
public record FileDownloadDto(
    Resource resource,
    String originalFileName,
    String contentType) {
}
