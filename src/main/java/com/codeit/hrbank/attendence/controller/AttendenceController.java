package com.codeit.hrbank.attendence.controller;

import com.codeit.hrbank.attendence.dto.request.CreateAttendenceRequest;
import com.codeit.hrbank.attendence.dto.response.AttendenceDto;
import com.codeit.hrbank.attendence.service.AttendenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attendances")
@Tag(name = "Attendance", description = "직원 출결 관리 API")
public class AttendenceController {

  private final AttendenceService attendenceService;

  @GetMapping
  @Operation(summary = "출결 목록 조회", description = "지정한 기간의 출결 기록을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공",
          content = @Content(array = @ArraySchema(schema = @Schema(implementation = AttendenceDto.class)))),
      @ApiResponse(responseCode = "400", description = "잘못된 날짜 형식 또는 조회 기간", content = @Content)
  })
  public ResponseEntity<List<AttendenceDto>> findAttendences(
      @Parameter(description = "조회 시작일", required = true)
      @RequestParam LocalDate startDate,
      @Parameter(description = "조회 종료일", required = true)
      @RequestParam LocalDate endDate
  ) {
    return ResponseEntity.ok(attendenceService.findAttendences(startDate, endDate));
  }

  @PostMapping
  @Operation(summary = "출결 등록", description = "직원의 출결 기록을 등록합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "등록 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
      @ApiResponse(responseCode = "404", description = "직원을 찾을 수 없음", content = @Content),
      @ApiResponse(responseCode = "409", description = "중복된 출결 기록", content = @Content)
  })
  public ResponseEntity<AttendenceDto> createAttendence(
      @Valid @RequestBody CreateAttendenceRequest request
  ) {
    AttendenceDto created = attendenceService.createAttendence(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @PatchMapping("/{id}")
  @Operation(summary = "출결 수정", description = "출결 기록을 수정합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "수정 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
      @ApiResponse(responseCode = "404", description = "출결 기록 또는 직원을 찾을 수 없음", content = @Content),
      @ApiResponse(responseCode = "409", description = "중복된 출결 기록", content = @Content)
  })
  public ResponseEntity<AttendenceDto> updateAttendence(
      @Parameter(description = "출결 ID", required = true) @PathVariable Long id,
      @Valid @RequestBody CreateAttendenceRequest request
  ) {
    return ResponseEntity.ok(attendenceService.updateAttendence(id, request));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "출결 삭제", description = "출결 기록을 삭제합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "삭제 성공"),
      @ApiResponse(responseCode = "404", description = "출결 기록을 찾을 수 없음", content = @Content)
  })
  public ResponseEntity<Void> deleteAttendence(
      @Parameter(description = "출결 ID", required = true) @PathVariable Long id) {
    attendenceService.deleteAttendence(id);
    return ResponseEntity.noContent().build();
  }
}
