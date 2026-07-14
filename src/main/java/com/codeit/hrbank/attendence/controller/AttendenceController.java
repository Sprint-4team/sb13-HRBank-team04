package com.codeit.hrbank.attendence.controller;

import com.codeit.hrbank.attendence.dto.request.CreateAttendenceRequest;
import com.codeit.hrbank.attendence.dto.response.AttendenceDto;
import com.codeit.hrbank.attendence.service.AttendenceService;
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
public class AttendenceController {

  private final AttendenceService attendenceService;

  @GetMapping
  public ResponseEntity<List<AttendenceDto>> findAttendences(
      @RequestParam LocalDate startDate,
      @RequestParam LocalDate endDate
  ) {
    return ResponseEntity.ok(attendenceService.findAttendences(startDate, endDate));
  }

  @PostMapping
  public ResponseEntity<AttendenceDto> createAttendence(
      @Valid @RequestBody CreateAttendenceRequest request
  ) {
    AttendenceDto created = attendenceService.createAttendence(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<AttendenceDto> updateAttendence(
      @PathVariable Long id,
      @Valid @RequestBody CreateAttendenceRequest request
  ) {
    return ResponseEntity.ok(attendenceService.updateAttendence(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteAttendence(@PathVariable Long id) {
    attendenceService.deleteAttendence(id);
    return ResponseEntity.noContent().build();
  }
}
