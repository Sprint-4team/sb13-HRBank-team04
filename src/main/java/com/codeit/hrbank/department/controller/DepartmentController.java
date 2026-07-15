package com.codeit.hrbank.department.controller;

import com.codeit.hrbank.department.dto.*;
import com.codeit.hrbank.department.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "부서 관리", description = "부서 등록, 수정, 삭제, 조회 API")

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @Operation(summary = "부서 등록", description = "이름, 설명, 설립일을 입력해 새로운 부서를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "이미 존재하는 부서 이름이거나 요청 값이 유효하지 않음")
    })
    @PostMapping
    public ResponseEntity<DepartmentDto> create(@Valid @RequestBody DepartmentCreateRequest request) {
        DepartmentDto response = departmentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "부서 정보 수정", description = "부서의 이름, 설명, 설립일을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "이미 존재하는 부서 이름이거나 요청 값이 유효하지 않음"),
            @ApiResponse(responseCode = "404", description = "해당 ID의 부서가 존재하지 않음")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<DepartmentDto> update(
            @Parameter(description = "수정할 부서의 ID", example = "1")
            @PathVariable("id") Long id,
            @Valid @RequestBody DepartmentUpdateRequest request
    ) {
        DepartmentDto response = departmentService.update(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "부서 삭제", description = "소속된 직원이 없는 경우에만 부서를 삭제할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "400", description = "소속된 직원이 있어 삭제할 수 없음"),
            @ApiResponse(responseCode = "404", description = "해당 ID의 부서가 존재하지 않음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "삭제할 부서의 ID", example = "1")
            @PathVariable("id") Long id
    ) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "부서 상세 조회", description = "부서 ID로 부서의 상세 정보(이름, 설명, 설립일, 소속 직원 수)를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 ID의 부서가 존재하지 않음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDto> find(
            @Parameter(description = "조회할 부서의 ID", example = "1")
            @PathVariable("id") Long id
    ) {
        DepartmentDto response = departmentService.find(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "부서 목록 조회", description = "이름 또는 설명으로 부서 목록을 검색하고, 이름/설립일 기준으로 정렬 및 커서 기반 페이지네이션을 지원합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<CursorPageResponseDepartmentDto> findAll(
            @Parameter(description = "이름 또는 설명 검색어 (부분 일치)")
            @RequestParam(required = false) String nameOrDescription,
            @Parameter(description = "이전 페이지 마지막 요소 ID")
            @RequestParam(required = false) Long idAfter,
            @Parameter(description = "커서 (다음 페이지 시작점)")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 필드 (name 또는 establishedDate)", example = "establishedDate")
            @RequestParam(defaultValue = "establishedDate") String sortField,
            @Parameter(description = "정렬 방향 (asc 또는 desc)", example = "asc")
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        DepartmentSearchCondition condition = new DepartmentSearchCondition(
                nameOrDescription, idAfter, cursor, size, sortField, direction
        );
        CursorPageResponseDepartmentDto response = departmentService.findAll(condition);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}