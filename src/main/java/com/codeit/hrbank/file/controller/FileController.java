package com.codeit.hrbank.file.controller;

import com.codeit.hrbank.file.dto.FileDownloadDto;
import com.codeit.hrbank.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
@Tag(name = "파일 관리", description = "파일 관리 API")
public class FileController {
  // 요청, 응답만 담당

  private final FileService fileService;

  // 다운로드 API - 백업, 프로필, 로그
  @Operation(summary = "파일 다운로드", description = "사용자의 프로필 이미지, 데이터 백업, 에러 로그 파일을 다운로드할 수 있습니다.")
  @ApiResponses(value={
      @ApiResponse(
          responseCode = "200", description = "다운로드 성공"
      ),
      @ApiResponse(
          responseCode = "404", description = "파일을 찾을 수 없음",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      ),
      @ApiResponse(
          responseCode = "500", description = "파일 경로가 유효하지 않거나 파일 저장소 문제",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @GetMapping("/{id}/download")
  public ResponseEntity<Resource> downloadFile(@PathVariable("id") Long id) {
    FileDownloadDto file = fileService.downloadFile(id);
    // findFile - downloadFile 의 findFile 반복되는 쿼리는 dto를 반환해 해결한다
    String encodedFileName = UriUtils.encode(file.originalFileName(), StandardCharsets.UTF_8);
    // 인코딩, 한글 깨짐 방지

    return ResponseEntity
        .status(HttpStatus.OK)
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\""
                + encodedFileName // 순수 한글 파일명 문제가 발생해 원본 파일명을 그대로 받지 않고 인코딩된 파일명을 받아 해결
                + "\"; filename*=UTF-8''" + encodedFileName
        ) // 최신 브라우저는 "filename*=" 인식, 구형 브라우저는 이를 인식 못 할 수도 있기 때문에 "filename=\" 추가
        .contentType(MediaType.parseMediaType(file.contentType()))
        .body(file.resource());
  }

}